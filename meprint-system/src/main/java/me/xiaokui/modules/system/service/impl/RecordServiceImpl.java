package me.xiaokui.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import me.xiaokui.constants.SystemConstant;
import me.xiaokui.modules.mapper.ExecRecordMapper;
import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.system.domain.User;
import me.xiaokui.modules.system.repository.UserRepository;
import me.xiaokui.modules.system.domain.request.record.*;
import me.xiaokui.modules.system.domain.request.ws.RecordWsClearReq;
import me.xiaokui.modules.system.domain.response.controller.PageModule;
import me.xiaokui.modules.system.domain.response.records.RecordGeneralInfoResp;
import me.xiaokui.modules.system.domain.response.records.RecordListResp;
import me.xiaokui.modules.system.domain.xmind.IntCount;
import me.xiaokui.modules.system.handler.Room;
import me.xiaokui.modules.system.handler.WebSocket;
import me.xiaokui.modules.system.service.RecordService;
import me.xiaokui.modules.system.service.dto.MergeCaseDto;
import me.xiaokui.modules.system.service.dto.PickCaseDto;
import me.xiaokui.modules.util.BitBaseUtil;
import me.xiaokui.modules.util.TimeUtil;
import me.xiaokui.modules.util.TreeUtil;
import me.xiaokui.modules.util.enums.EnvEnum;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.system.service.dto.RecordWsDto;
import me.xiaokui.modules.persistent.ExecRecord;
import me.xiaokui.modules.persistent.TestCase;
import me.xiaokui.modules.system.domain.request.record.RecordAddReq;
import me.xiaokui.modules.system.domain.request.record.RecordUpdateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * ?????????????????????
 *
 * @author didi
 * @date 2020/9/23
 */
@Service
public class RecordServiceImpl implements RecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordServiceImpl.class);

    private static final String OE_PICK_ALL = "\"priority\":[\"0\"]";

    private static final Integer DEFAULT_ENV = 0;

    @Autowired
    private ExecRecordMapper recordMapper;

    @Autowired
    private TestCaseMapper caseMapper;

    @Autowired
    private UserRepository userRepository;

    @Override
    public PageModule<RecordListResp> getListByCaseId(RecordQueryReq req) {
        List<RecordListResp> res = new ArrayList<>();
        List<ExecRecord> execRecordList = null;
        User user = userRepository.findByUsername(req.getUserName());
        TestCase testCase = caseMapper.selectOne(req.getCaseId());
        if (testCase == null) {
            throw new CaseServerException("???????????????", StatusCode.NOT_FOUND_ENTITY);
        }
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        if (user.getIsAdmin().equals(true)) {
            execRecordList = recordMapper.getRecordListByCaseId(req.getCaseId());
        } else {
            execRecordList = recordMapper.getRecordListByCaseIdAndUserName(req.getCaseId(),req.getUserName());
        }

        for (ExecRecord record : execRecordList) {
            res.add(buildList(record));
        }
        return PageModule.buildPage(res, ((Page<ExecRecord>) execRecordList).getTotal());
    }

    @Override
    public PageModule<RecordListResp> getList(RecordQueryReq req) {
        List<RecordListResp> res = new ArrayList<>();
        List<ExecRecord> execRecordList = null;
        User user = null;
        Date beginTime;
        Date endTime;

        if (req.getPageNum() !=null && req.getPageSize() !=null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        if (req.getUserName() !=null ) {
            user = userRepository.findByUsername(req.getUserName());
            if (req.getTitle() ==null && req.getOwner() ==null && req.getExpectStartTime() ==null && req.getExpectEndTime() ==null) {
                execRecordList = recordMapper.selectAll();
            }else if (user.getIsAdmin().equals(false) && req.getTitle() ==null && req.getOwner() ==null && req.getExpectStartTime() ==null && req.getExpectEndTime() ==null){
                execRecordList = recordMapper.selectAllByOwner(req.getUserName());
            }
        }

        if (req.getTitle() !=null || req.getOwner() !=null || (req.getExpectStartTime() !=null && req.getExpectEndTime() !=null)) {
            beginTime = transferTime(req.getExpectStartTime());
            endTime = transferTime(req.getExpectEndTime());
            execRecordList = recordMapper.search(req.getTitle(), req.getOwner(), beginTime, endTime);
        }
//        else if (user.getIsAdmin().equals(false) && (req.getTitle() !=null || req.getOwner() !=null || (req.getExpectStartTime() !=null && req.getExpectEndTime() !=null))){
//            beginTime = transferTime(req.getExpectStartTime());
//            endTime = transferTime(req.getExpectEndTime());
//            execRecordList = recordMapper.searchByOne(req.getTitle(), req.getOwner(), beginTime, endTime);
//        }

        for (ExecRecord record : execRecordList) {
            res.add(buildList(record));
        }

        return PageModule.buildPage(res, ((Page<ExecRecord>) execRecordList).getTotal());
    }

    @Override
    public RecordGeneralInfoResp getGeneralInfo(Long recordId) {
        ExecRecord record = recordMapper.selectOne(recordId);
        if (record == null) {
            throw new CaseServerException("?????????????????????", StatusCode.NOT_FOUND_ENTITY);
        }

        TestCase testCase = caseMapper.selectOne(record.getCaseId());
        JSONObject merged = getData(new MergeCaseDto(testCase.getId(), record.getChooseContent(), record.getCaseContent(), record.getEnv(), 0L));

        // ?????????????????????
        return buildGeneralInfoResp(record, testCase, merged);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRecord(RecordAddReq req) {
        // ??????caseContent???recordContent??????????????????????????????
        JSONObject merged = getData(new MergeCaseDto(req.getCaseId(), req.getChooseContent(), SystemConstant.EMPTY_STR, DEFAULT_ENV, 0L));
        ExecRecord record = buildExecRecord(req, merged);
        return recordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long recordId) {
        recordMapper.delete(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editRecord(RecordUpdateReq req) {
        // ?????????????????? ??????????????????content??????????????????patch???????????????????????????
        // ?????????????????????????????????????????? ????????????websocket???redis??????
        ExecRecord record = recordMapper.selectOne(req.getId());
        if (record == null) {
            throw new CaseServerException("???????????????????????????", StatusCode.NOT_FOUND_ENTITY);
        }
        // ??????????????????????????????????????????????????????????????????????????????????????????
        if (!record.getChooseContent().equals(req.getChooseContent())) {
            Room room = getWsEditingCount(record);
            if (room != null && room.players.size() > 0) {
                LOGGER.info("[????????????????????????]??????={}, ?????????????????????={}", req.toString(), room.getRoomPlayersName());
                throw new CaseServerException("??????" + room.getRoomPlayersName() + "??????????????????????????????????????????", StatusCode.INTERNAL_ERROR);
            }
        }
        // ???????????????,???request.validate()???????????????????????????null??????????????????
        if (req.getExpectStartTime() != null) {
            // ????????????????????????????????????
            record.setExpectStartTime(new Date(req.getExpectStartTime()/1000*1000));
            record.setExpectEndTime(new Date(req.getExpectEndTime()/1000*1000));
        } else {
            // ??????????????????????????? 1970-01-01 00:00:00
            record.setExpectStartTime(new Date(31507200000L));
            record.setExpectEndTime(new Date(31507200000L));
        }

        record.setModifier(req.getModifier());
        record.setTitle(req.getTitle());
        record.setChooseContent(req.getChooseContent());
        record.setDescription(StringUtils.isEmpty(req.getDescription()) ? SystemConstant.EMPTY_STR : req.getDescription());
        record.setOwner(StringUtils.isEmpty(req.getOwner()) ? SystemConstant.EMPTY_STR : req.getOwner());

        recordMapper.edit(record);
    }

    @Override
    public RecordWsDto getWsRecord(Long recordId) {
        ExecRecord record = recordMapper.selectOne(recordId);
        if (record == null) {
            throw new CaseServerException("?????????????????????", StatusCode.NOT_FOUND_ENTITY);
        }
        RecordWsDto dto = new RecordWsDto();
        dto.setCaseContent(record.getCaseContent());
        dto.setChooseContent(record.getChooseContent());
        dto.setEnv(record.getEnv());
        dto.setExecutors(record.getExecutors());
        dto.setUpdateTime(record.getGmtModified());

        return dto;
    }

    @Override
    public void modifyRecord(ExecRecord record) {
        if (record == null) {
            throw new CaseServerException("???????????????????????????", StatusCode.NOT_FOUND_ENTITY);
        }
        if (StringUtils.isEmpty(record.getCaseContent())) {
            throw new CaseServerException("??????/??????????????????", StatusCode.INTERNAL_ERROR);
        }
        if (StringUtils.isEmpty(record.getModifier())) {
            throw new CaseServerException("???????????????", StatusCode.INTERNAL_ERROR);
        }
        ExecRecord dbRecord = recordMapper.selectOne(record.getId());
        BeanUtils.copyProperties(record, dbRecord);
        recordMapper.update(dbRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecRecord wsClearRecord(RecordWsClearReq req) {
        ExecRecord record = recordMapper.selectOne(req.getId());
        record.setCaseContent(SystemConstant.EMPTY_STR);
        record.setSuccessCount(0);
        record.setPassCount(0);
        record.setFailCount(0);
        record.setBlockCount(0);
        record.setIgnoreCount(0);
        record.setModifier(req.getModifier());
        record.setGmtModified(new Date());

        recordMapper.update(record);

        JSONObject merged = getData(new MergeCaseDto(record.getCaseId(), record.getChooseContent(), record.getCaseContent(), record.getEnv(), record.getId()));
        record.setCaseContent(merged.get("content").toString());
        return record;
    }

    /**
     * ????????????
     *
     * @param record ??????????????????
     * @return ?????????
     */
    private RecordListResp buildList(ExecRecord record) {
        RecordListResp resp = new RecordListResp();
        resp.setId(record.getId());
        resp.setRecordId(record.getId());
        resp.setCaseId(record.getCaseId());
        resp.setTitle(record.getTitle());
        resp.setOwner(record.getOwner());
        resp.setCreator(record.getCreator());
        resp.setExecutors(record.getExecutors());

        // ?????????????????????????????????????????????????????????????????????chooseContent
        JSONObject object = getData(new MergeCaseDto(record.getCaseId(), record.getChooseContent(), record.getCaseContent(), record.getEnv(), 0L));
        resp.setBugNum(object.getInteger("failCount"));
        resp.setBlockNum(object.getInteger("blockCount"));
        resp.setSuccessNum(object.getInteger("successCount"));
        resp.setExecuteNum(object.getInteger("passCount"));
        resp.setTotalNum(object.getInteger("totalCount"));
        resp.setChooseContent(record.getChooseContent());
        resp.setCreateTime(record.getGmtCreated().getTime());
        resp.setDescription(record.getDescription());
        resp.setExpectStartTime(
                TimeUtil.compareToOriginalDate(record.getExpectStartTime()) ? null : record.getExpectStartTime());
        resp.setExpectEndTime(
                TimeUtil.compareToOriginalDate(record.getExpectEndTime()) ? null : record.getExpectEndTime());
        return resp;
    }

    /**
     * ??????????????????
     *
     * @param req ?????????
     * @param merged ??????????????????
     * @return
     */
    private ExecRecord buildExecRecord(RecordAddReq req, JSONObject merged) {
        ExecRecord record = new ExecRecord();
        // ??????????????????
        record.setFailCount(merged.getInteger("failCount"));
        record.setBlockCount(merged.getInteger("blockCount"));
        record.setSuccessCount(merged.getInteger("successCount"));
        record.setPassCount(merged.getInteger("passCount"));
        record.setIgnoreCount(merged.getInteger("ignoreCount"));
        record.setTotalCount(merged.getInteger("totalCount"));
        // ??????????????????
        record.setTitle(req.getTitle());
        record.setCaseId(req.getCaseId());
        record.setIsDelete(SystemConstant.NOT_DELETE);
        record.setCaseContent(SystemConstant.EMPTY_STR);
        record.setCreator(req.getCreator());
        record.setModifier(SystemConstant.EMPTY_STR);
        record.setGmtCreated(new Date());
        record.setGmtModified(new Date());
        record.setChooseContent(StringUtils.isEmpty(req.getChooseContent()) ? SystemConstant.EMPTY_STR : req.getChooseContent());
        record.setDescription(StringUtils.isEmpty(req.getDescription()) ? SystemConstant.EMPTY_STR : req.getDescription());
        record.setExecutors(SystemConstant.EMPTY_STR);
        record.setOwner(req.getOwner());
        record.setEnv(0);

        if (req.getExpectStartTime() != null) {
            // ????????????????????????????????????
            record.setExpectStartTime(new Date(req.getExpectStartTime()/1000*1000));
            record.setExpectEndTime(new Date(req.getExpectEndTime()/1000*1000));
        } else {
            // ???????????????????????????
            record.setExpectStartTime(new Date(31507200000L));
            record.setExpectEndTime(new Date(31507200000L));
        }
        return record;
    }

    /**
     * ???????????????
     *
     * @param record ??????????????????
     * @param testCase ??????
     * @param merged ????????????json???
     * @return ?????????
     */
    private RecordGeneralInfoResp buildGeneralInfoResp(ExecRecord record, TestCase testCase, JSONObject merged) {
        RecordGeneralInfoResp resp = new RecordGeneralInfoResp();
        resp.setId(record.getId());
        resp.setTitle(record.getTitle());
        resp.setCaseId(testCase.getId());
        resp.setRequirementIds(testCase.getRequirementId());
        resp.setExpectStartTime(
                TimeUtil.compareToOriginalDate(record.getExpectStartTime()) ? null : record.getExpectStartTime());
        resp.setExpectEndTime(
                TimeUtil.compareToOriginalDate(record.getExpectEndTime()) ? null : record.getExpectEndTime());
        if (merged.getInteger("totalCount") == 0 || merged.getInteger("totalCount") == null) {
            // RecordGeneralInfoResp ??????????????????????????????0
            return resp;
        }
        resp.setPassCount(merged.getInteger("passCount"));
        resp.setBugNum(merged.getInteger("failCount"));
        resp.setSuccessCount(merged.getInteger("successCount"));
        resp.setBlockCount(merged.getInteger("blockCount"));
        resp.setIgnoreCount(merged.getInteger("ignoreCount"));
        resp.setTotalCount(merged.getInteger("totalCount"));

        BigDecimal passRate = BigDecimal.valueOf((double) resp.getSuccessCount() * 100 / (double) resp.getTotalCount());
        //?????????=??????????????????/?????????
        resp.setPassRate(passRate.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        return resp;
    }

    /**
     * ?????????????????????????????????????????????  ????????????+??????
     *
     * @param record ??????????????????
     * @return ?????????
     */
    public Room getWsEditingCount(ExecRecord record) {
        TestCase testCase = caseMapper.selectOne(record.getCaseId());
        if (testCase == null) {
            throw new CaseServerException("?????????????????????", StatusCode.INTERNAL_ERROR);
        }

        return WebSocket.getRoom(false, BitBaseUtil.mergeLong(record.getId(), record.getCaseId()));
    }

    /**
     * ????????????record??????????????????????????????????????????merge???????????????????????????
     */
    public JSONObject getData(MergeCaseDto dto) {
        String websocketCaseContent = null;
        if (dto.getRecordId() > 0L) {
            websocketCaseContent = WebSocket.getRoom(false, BitBaseUtil.mergeLong(dto.getRecordId(), dto.getCaseId())).getTestCaseContent();
        }

        String caseContent = caseMapper.selectOne(dto.getCaseId()).getCaseContent();
        JSONObject content = JSON.parseObject(caseContent);
        if (websocketCaseContent != null) {
            JSONObject websocketContent = JSON.parseObject(websocketCaseContent);
            long currentBase = websocketContent.getLong("base");
            content.put("base", currentBase);
        }

        // ???????????????????????????????????????
        if (!StringUtils.isEmpty(dto.getChooseContent()) && !dto.getChooseContent().contains(OE_PICK_ALL)) {
            PickCaseDto pickCaseDto = JSON.parseObject(dto.getChooseContent(), PickCaseDto.class);

            // ???????????????BFS???????????????
            JSONObject caseRoot = content.getJSONObject("root");
            Stack<JSONObject> objCheck = new Stack<>();
            Stack<IntCount> iCheck = new Stack<>();
            objCheck.push(caseRoot);

            //????????????????????????
            if (!CollectionUtils.isEmpty(pickCaseDto.getPriority())) {
                TreeUtil.getPriority(objCheck, iCheck, caseRoot, pickCaseDto.getPriority());
            }
            if (!CollectionUtils.isEmpty(pickCaseDto.getResource())) {
                TreeUtil.getChosenCase(caseRoot, new HashSet<>(pickCaseDto.getResource()), "resources");
            }
        } else {
            // ????????????????????????????????????...
            if (EnvEnum.TestQaEnv.getValue().equals(dto.getEnv()) || EnvEnum.TestRdEnv.getValue().equals(dto.getEnv())) {
                // ???????????????BFS???????????????
                JSONObject caseRoot = content.getJSONObject("root");
                Stack<JSONObject> objCheck = new Stack<>();
                Stack<IntCount> iCheck = new Stack<>();
                objCheck.push(caseRoot);

                // ????????????????????????????????????
                TreeUtil.getPriority0(objCheck, iCheck, caseRoot);
            }
        }
        //????????????
        String recordContent = dto.getRecordContent();
        JSONObject recordObj = new JSONObject();

        if (StringUtils.isEmpty(recordContent)) {
            // ??????????????????
        } else if (recordContent.startsWith("[{")) {
            for (Object o : JSON.parseArray(recordContent)) {
                recordObj.put(((JSONObject) o).getString("id"), ((JSONObject) o).getLong("progress"));
            }
        } else {
            recordObj = JSON.parseObject(recordContent);
        }

        IntCount execCount = new IntCount(recordObj.size());
        TreeUtil.mergeExecRecord(content.getJSONObject("root"), recordObj, execCount);
        return TreeUtil.parse(content.toJSONString());
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
}
