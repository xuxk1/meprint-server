package me.xiaokui.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import me.xiaokui.constants.SystemConstant;
import me.xiaokui.modules.mapper.BizMapper;
import me.xiaokui.modules.mapper.ExecRecordMapper;
import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.persistent.Biz;
import me.xiaokui.modules.persistent.ExecRecord;
import me.xiaokui.modules.system.domain.request.cases.CaseConditionReq;
import me.xiaokui.modules.system.domain.request.cases.CaseEditReq;
import me.xiaokui.modules.system.domain.request.cases.CaseQueryReq;
import me.xiaokui.modules.system.domain.request.ws.WsSaveReq;
import me.xiaokui.modules.system.domain.response.PersonResp;
import me.xiaokui.modules.system.domain.response.cases.CaseConditionResp;
import me.xiaokui.modules.system.domain.response.cases.CaseDetailResp;
import me.xiaokui.modules.system.domain.response.cases.CaseGeneralInfoResp;
import me.xiaokui.modules.system.domain.response.cases.CaseListResp;
import me.xiaokui.modules.system.domain.response.dir.BizListResp;
import me.xiaokui.modules.system.domain.response.dir.DirTreeResp;
import me.xiaokui.modules.system.service.CaseBackupService;
import me.xiaokui.modules.system.service.CaseService;
import me.xiaokui.modules.system.service.DirService;
import me.xiaokui.modules.system.service.RecordService;
import me.xiaokui.modules.system.service.dto.DirNodeDto;
import me.xiaokui.modules.system.service.dto.RecordNumDto;
import me.xiaokui.modules.system.service.dto.RecordWsDto;
import me.xiaokui.modules.util.TimeUtil;
import me.xiaokui.modules.util.TreeUtil;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.persistent.CaseBackup;
import me.xiaokui.modules.persistent.TestCase;
import me.xiaokui.modules.system.domain.request.cases.CaseCreateReq;
import me.xiaokui.modules.system.domain.response.controller.PageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static me.xiaokui.constants.SystemConstant.COMMA;
import static me.xiaokui.constants.SystemConstant.IS_DELETE;

/**
 * ???????????????
 *
 * @author didi
 * @date 2020/9/7
 */
@Service
public class CaseServiceImpl implements CaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseServiceImpl.class);

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private DirService dirService;

    @Autowired
    private TestCaseMapper caseMapper;

    @Autowired
    private ExecRecordMapper recordMapper;

    @Autowired
    private RecordService recordService;

    @Autowired
    private CaseBackupService caseBackupService;

    @Override
    public PageModule<CaseListResp> getCaseList(CaseQueryReq request) {
        List<CaseListResp> res = new ArrayList<>();
        List<Long> caseIds;
        if (request.getProjectId()!=null){
            caseIds = dirService.getCaseIdLists(request.getProjectId(), request.getLineId(), request.getBizId(), request.getChannel());
        } else {
            caseIds = dirService.getCaseIds(request.getLineId(), request.getBizId(), request.getChannel());
        }

        if (CollectionUtils.isEmpty(caseIds)) {
            return PageModule.emptyPage();
        }

        Date beginTime = transferTime(request.getBeginTime());
        Date endTime = transferTime(request.getEndTime());
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        // select * from test_case where case_id in (request.getCaseIds()) [and ...any other condition];
        List<TestCase> caseList = caseMapper.search(request.getCaseType(), caseIds, request.getTitle(),
                request.getCreator(), request.getRequirementId(), beginTime, endTime, request.getChannel(), request.getLineId(), request.getProjectId());

        List<RecordNumDto> recordNumDtos = recordMapper.getRecordNumByCaseIds(caseIds);
        Map<Long, Integer> recordMap = recordNumDtos.stream().collect(Collectors.toMap(RecordNumDto::getCaseId, RecordNumDto::getRecordNum));

        for (TestCase testCase : caseList) {
            res.add(buildListResp(testCase, recordMap.get(testCase.getId())));
        }

        return PageModule.buildPage(res, ((Page<TestCase>) caseList).getTotal());
    }

    @Override
    public CaseDetailResp getCaseDetail(Long caseId) {
        TestCase testCase = caseMapper.selectOne(caseId);
        if (testCase == null) {
            throw new CaseServerException("???????????????", StatusCode.INTERNAL_ERROR);
        }
        if (testCase.getIsDelete().equals(IS_DELETE)) {
            throw new CaseServerException("???????????????", StatusCode.INTERNAL_ERROR);
        }
        return buildDetailResp(testCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertOrDuplicateCase(CaseCreateReq request) {
        TestCase testcase = buildCase(request);
        caseMapper.insert(testcase);
        // ?????????????????????  ??????????????????dirService.addCase()
        DirNodeDto tree = dirService.getDirTree(testcase.getProductLineId(), testcase.getChannel());
        List<String> addBizs = Arrays.asList(request.getBizId().split(SystemConstant.COMMA));
        updateDFS(packageTree(tree), String.valueOf(testcase.getId()), new HashSet<>(addBizs), new HashSet<>());
        updateBiz(testcase, tree);

        return testcase.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DirTreeResp updateCase(CaseEditReq request) {
        TestCase testCase = caseMapper.selectOne(request.getId());
        if (testCase == null) {
            throw new CaseServerException("???????????????", StatusCode.NOT_FOUND_ENTITY);
        }

        List<String> addBizs = getDiffSet(request.getBizId(), testCase.getBizId());
        List<String> rmBizs = getDiffSet(testCase.getBizId(), request.getBizId());

        BeanUtils.copyProperties(request, testCase);
        testCase.setGmtModified(new Date());

        DirNodeDto tree = new DirNodeDto();
        if (request.getProjectId() !=null){
            tree = dirService.getProjectTree(testCase.getProjectId(), testCase.getProductLineId(), testCase.getChannel());
        } else {
            tree = dirService.getDirTree(testCase.getProductLineId(), testCase.getChannel());
        }
        updateDFS(packageTree(tree), String.valueOf(request.getId()), new HashSet<>(addBizs), new HashSet<>(rmBizs));
        updateBiz(testCase, tree);

        caseMapper.update(testCase);

        return dirService.getAllCaseDir(tree);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DirTreeResp deleteCase(Long caseId) {
        TestCase testCase = caseMapper.selectOne(caseId);
        testCase.setIsDelete(IS_DELETE);

        // ????????????????????????
        List<ExecRecord> execRecords = recordMapper.getRecordListByCaseId(testCase.getId());
        if (!CollectionUtils.isEmpty(execRecords)) {
            recordMapper.batchDelete(execRecords.stream().map(ExecRecord::getId).collect(Collectors.toList()));
        }

        DirNodeDto tree = dirService.getDirTree(testCase.getProductLineId(), testCase.getChannel());
        updateDFS(packageTree(tree), String.valueOf(caseId), new HashSet<>(), new HashSet<>(convertToList(testCase.getBizId())));
        updateBiz(testCase, tree);

        caseMapper.delete(testCase.getId());
        return dirService.getAllCaseDir(tree);
    }

    @Override
    public List<PersonResp> listCreators(Integer caseType, Long lineId) {
        List<PersonResp> list = new ArrayList<>();
        List<String> names = caseMapper.listCreators(caseType, lineId);

        if (CollectionUtils.isEmpty(names)) {
            return list;
        }

        return names.stream().map(name -> {
                    PersonResp person = new PersonResp();
                    person.setStaffNamePY(name);
                    // ???????????????????????????????????????????????????????????????
                    person.setStaffNameCN(name);
                    return person;
                }).collect(Collectors.toList());
    }


    @Override
    public CaseConditionResp getCountByCondition(CaseConditionReq req) {
        CaseConditionResp res = new CaseConditionResp();

        TestCase testCase = caseMapper.selectOne(req.getCaseId());
        JSONObject content = JSONObject.parseObject(testCase.getCaseContent());
        JSONObject caseRoot = content.getJSONObject("root");

        HashSet<String> tags = new HashSet<>();
        Integer caseNum = TreeUtil.getCaseNum(caseRoot, tags);

        res.setTotalCount(caseNum);
        res.setTaglist(tags);

        HashSet<String> prioritySet, resourceSet;
        if (!CollectionUtils.isEmpty(req.getPriority())) {
            prioritySet = new HashSet<>(req.getPriority());
            if (!TreeUtil.getChosenCase(caseRoot, prioritySet, "priority")) {
                caseRoot = null;
            }
        }
        if (!CollectionUtils.isEmpty(req.getResource())) {
            resourceSet = new HashSet<>(req.getResource());
            if (!TreeUtil.getChosenCase(caseRoot, resourceSet, "resource")) {
                caseRoot = null;
            }
        }
        //?????????????????????caseNum???null
        caseNum = (req.getPriority().size() == 0 && req.getResource().size() == 0) ? null : TreeUtil.getCaseNum(caseRoot, tags);
        res.setCount(caseNum);
        return res;
    }

    @Override
    public CaseGeneralInfoResp getCaseGeneralInfo(Long caseId) {
        TestCase testCase = caseMapper.selectOne(caseId);
        if (testCase == null) {
            throw new CaseServerException("???????????????", StatusCode.NOT_FOUND_ENTITY);
        }
        CaseGeneralInfoResp resp = new CaseGeneralInfoResp();
        resp.setId(testCase.getId());
        resp.setProductLineId(testCase.getProductLineId());
        resp.setRequirementId(testCase.getRequirementId());
        resp.setTitle(testCase.getTitle());
        return resp;
    }

    @Override
    public void wsSave(WsSaveReq req) {
//        List<String> editors = WebSocket.getEditingUser(String.valueOf(req.getId()),
//                StringUtils.isEmpty(req.getRecordId())?"undefined":String.valueOf(req.getRecordId()));
//        if (editors.size() < 1) {
//            throw new CaseServerException("??????ws?????????????????????????????????????????????????????????????????????ws?????????", StatusCode.WS_UNKNOWN_ERROR);
//        }

        CaseBackup caseBackup = new CaseBackup();
        // ??????????????????record
        if (!StringUtils.isEmpty(req.getRecordId())) {
            RecordWsDto dto = recordService.getWsRecord(req.getRecordId());
            // ????????????????????????????????????
            List<String> names = Arrays.stream(dto.getExecutors().split(COMMA)).filter(e->!StringUtils.isEmpty(e)).collect(Collectors.toList());
            long count = names.stream().filter(e -> e.equals(req.getModifier())).count();
            String executors;
            if (count > 0) {
                // ?????????????????????
                executors = dto.getExecutors();
            } else {
                // ???????????????????????????
                names.add(req.getModifier());
                executors = String.join(",", names);
            }

            JSONObject jsonObject = TreeUtil.parse(req.getCaseContent());
            ExecRecord record = new ExecRecord();
            record.setId(req.getRecordId());
            record.setCaseId(req.getId());
            record.setModifier(req.getModifier());
            record.setGmtModified(new Date(System.currentTimeMillis()));
            record.setCaseContent(jsonObject.getJSONObject("progress").toJSONString());
            record.setFailCount(jsonObject.getInteger("failCount"));
            record.setBlockCount(jsonObject.getInteger("blockCount"));
            record.setIgnoreCount(jsonObject.getInteger("ignoreCount"));
            record.setPassCount(jsonObject.getInteger("passCount"));
            record.setTotalCount(jsonObject.getInteger("totalCount"));
            record.setSuccessCount(jsonObject.getInteger("successCount"));
            record.setExecutors(executors);
            recordService.modifyRecord(record);
            caseBackup.setCaseId(req.getRecordId());
            caseBackup.setRecordContent(req.getCaseContent());
            caseBackup.setCaseContent("");
        } else {
            // ??????????????????testcase
            TestCase testCase = caseMapper.selectOne(req.getId());
            testCase.setCaseContent(req.getCaseContent());
            testCase.setModifier(req.getModifier());
            caseMapper.update(testCase);
            caseBackup.setCaseId(req.getId());
            caseBackup.setCaseContent(req.getCaseContent());
            caseBackup.setRecordContent("");

        }
        caseBackup.setCreator(req.getModifier());
        caseBackup.setExtra("");
        caseBackupService.insertBackup(caseBackup);
    }

    /**
     * ??????????????????date
     *
     * @param time ???????????????
     * @return ??????????????????????????????Date?????????
     */
    private Date transferTime(String time) {
        if (time == null) {
            return null;
        }
        return TimeUtil.transferStrToDateInSecond(time);
    }

    private List<String> getDiffSet(String newStr, String oldStr) {
        List<String> newIds = convertToList(newStr);
        List<String> oldIds = convertToList(oldStr);
        newIds.removeIf(oldIds::contains);
        return newIds;
    }

    private List<String> convertToList(String str) {
        return Arrays.stream(str.split(SystemConstant.COMMA)).collect(Collectors.toList());
    }

    /**
     * ??????/list??????????????????
     *
     * @param testCase ????????????
     * @return ????????????
     * @see #getCaseList
     */
    private CaseListResp buildListResp(TestCase testCase, Integer recordNum) {
        CaseListResp resp = new CaseListResp();
        BeanUtils.copyProperties(testCase, resp);
        resp.setRecordNum(recordNum == null ? 0 : recordNum);
        return resp;
    }

    /**
     * ????????????????????????
     *
     * @param testCase ????????????
     * @return ????????????
     * @see #getCaseDetail
     */
    private CaseDetailResp buildDetailResp(TestCase testCase) {
        CaseDetailResp resp = new CaseDetailResp();
        BeanUtils.copyProperties(testCase, resp);
        resp.setBiz(
                getBizFlatList(testCase.getProductLineId(), Arrays.asList(testCase.getBizId().split(SystemConstant.COMMA)), testCase.getChannel())
                        .stream().filter(BizListResp::isSelect).collect(Collectors.toList())
        );
        resp.setProductLineId(testCase.getProductLineId());
        return resp;
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param lineId ?????????id
     * @param bizIds ??????????????????id??????
     * @return ????????????????????????????????????
     * @see #buildDetailResp
     */
    private List<BizListResp> getBizFlatList(Long lineId, List<String> bizIds, Integer channel) {
        DirNodeDto root = dirService.getDirTree(lineId, channel);
        List<BizListResp> list = new ArrayList<>();
        flatDfs(root, list, new ArrayList<>(), bizIds);
        // ????????????root???????????????
        list.remove(0);
        return list;
    }

    private void flatDfs(DirNodeDto node, List<BizListResp> list, List<String> path, List<String> bizIds) {
        list.add(buildBizList(node, path, bizIds));

        if (CollectionUtils.isEmpty(node.getChildren())) {
            return ;
        }

        for (int i = 0; i < node.getChildren().size(); i++) {
            path.add(node.getChildren().get(i).getText());
            flatDfs(node.getChildren().get(i), list, path, bizIds);
            path.remove(path.size() - 1);
        }
    }

    private BizListResp buildBizList(DirNodeDto node, List<String> path, List<String> bizIds) {
        BizListResp obj = new BizListResp();
        obj.setBizId(node.getId());
        obj.setText(String.join(">", path));
        obj.setSelect(bizIds.contains(node.getId()));
        return obj;
    }

    /**
     * ??????/??????????????????????????????
     *
     * @param request ?????????
     * @return ???????????????
     * @see #insertOrDuplicateCase
     */
    private TestCase buildCase(CaseCreateReq request) {
        String content = request.getCaseContent();
        // ???????????????
        if (request.getId() != null) {
            TestCase testCase = caseMapper.selectOne(request.getId());
            if (testCase == null) {
                throw new CaseServerException("???????????????", StatusCode.NOT_FOUND_ENTITY);
            }
            content = testCase.getCaseContent();
        }

        TestCase ret = new TestCase();
        ret.setTitle(request.getTitle());
        ret.setRequirementId(request.getRequirementId());
        ret.setBizId(request.getBizId());
        ret.setProjectId(request.getProjectId());
        ret.setGroupId(1L);
        ret.setProductLineId(request.getProductLineId());
        ret.setDescription(request.getDescription());
        ret.setCreator(request.getCreator());
        ret.setModifier(ret.getCreator());
        ret.setChannel(request.getChannel());
        ret.setExtra(SystemConstant.EMPTY_STR);
        ret.setGmtCreated(new Date());
        ret.setGmtModified(new Date());
        ret.setCaseContent(content);
        ret.setCaseType(request.getCaseType());
        return ret;
    }

    /**
     * ??????json???
     *
     * @param node ???
     * @param addSet ????????????caseId???set
     * @param rmSet ????????????caseId???set
     */
    private void updateDFS(DirNodeDto node, String caseId, Set<String> addSet, Set<String> rmSet) {
        if (CollectionUtils.isEmpty(node.getChildren())) {
            return ;
        }

        for (int i = 0; i < node.getChildren().size(); i++) {
            DirNodeDto childNode = node.getChildren().get(i);
            if (addSet.contains(childNode.getId())) {
                childNode.getCaseIds().add(caseId);
            }
            if (rmSet.contains(childNode.getId())) {
                childNode.getCaseIds().remove(caseId);
            }
            updateDFS(childNode, caseId, addSet, rmSet);
        }
    }

    /**
     * dir-?????????????????????????????????????????????????????????????????????
     */
    private DirNodeDto packageTree(DirNodeDto node) {
        DirNodeDto pack = new DirNodeDto();
        pack.getChildren().add(node);
        return pack;
    }

    /**
     * ???????????????
     *
     * @param testCase ????????????
     * @param tree ???
     */
    public void updateBiz(TestCase testCase, DirNodeDto tree) {
        Biz biz = bizMapper.selectOne(testCase.getProductLineId(), testCase.getChannel());
        biz.setContent(JSON.toJSONString(tree));
        biz.setGmtModified(new Date());
        bizMapper.update(biz);
    }
}
