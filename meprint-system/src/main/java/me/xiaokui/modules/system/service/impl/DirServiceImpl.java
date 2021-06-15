package me.xiaokui.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import me.xiaokui.constants.BizConstant;
import me.xiaokui.modules.mapper.BizMapper;
import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.system.domain.request.dir.DirCreateReq;
import me.xiaokui.modules.system.domain.request.dir.DirDeleteReq;
import me.xiaokui.modules.system.domain.request.dir.DirRenameReq;
import me.xiaokui.modules.system.domain.response.dir.DirTreeResp;
import me.xiaokui.modules.system.service.DirService;
import me.xiaokui.modules.system.service.ProjectService;
import me.xiaokui.modules.system.service.dto.DirNodeDto;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.system.rest.CaseController;
import me.xiaokui.modules.persistent.Biz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件夹实现类
 *
 * @author hcy
 * @date 2020/11/24
 */
@Service
public class DirServiceImpl implements DirService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseController.class);

    @Autowired
    BizMapper bizMapper;

    @Autowired
    TestCaseMapper caseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DirNodeDto addDir(DirCreateReq request) {
        DirNodeDto root = getDirTree(request.getProductLineId(), request.getChannel());
        checkNodeExists(request.getText(), request.getParentId(), root);
        DirNodeDto dir = getDir(request.getParentId(), root);
        if (dir == null) {
            throw new CaseServerException("目录节点获取为空", StatusCode.INTERNAL_ERROR);
        }

        List<DirNodeDto> children = dir.getChildren();
        DirNodeDto newDir = new DirNodeDto();
        newDir.setId(UUID.randomUUID().toString().substring(0,8));
        newDir.setText(request.getText());
        newDir.setParentId(dir.getId());
        children.add(newDir);

        bizMapper.updateContent(request.getProductLineId(), JSONObject.toJSONString(root), request.getChannel());
        return root;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DirNodeDto renameDir(DirRenameReq request) {
        DirNodeDto dirTree = getDirTree(request.getProductLineId(), request.getChannel());
        if (!BizConstant.ROOT_BIZ_ID.equalsIgnoreCase(request.getId())) {
            String parentId = getParentIdWithRecursion(request.getId(), dirTree);
            if (null != parentId) {
                checkNodeExists(request.getText(), parentId, dirTree);
            }
        }

        DirNodeDto root = getDirTree(request.getProductLineId(), request.getChannel());
        DirNodeDto dir = getDir(request.getId(), root);
        if (dir == null) {
            throw new CaseServerException("目录节点获取为空", StatusCode.INTERNAL_ERROR);
        }

        dir.setText(request.getText());
        bizMapper.updateContent(request.getProductLineId(), JSONObject.toJSONString(root), request.getChannel());
        return root;
    }



    @Override
    public DirNodeDto delDir(DirDeleteReq request) {
        DirNodeDto root = getDirTree(request.getProductLineId(), request.getChannel());
        DirNodeDto dir = getDir(request.getParentId(), root);
        if (dir == null) {
            throw new CaseServerException("目录节点获取为空", StatusCode.INTERNAL_ERROR);
        }

        Iterator<DirNodeDto> iterator = dir.getChildren().iterator();
        while (iterator.hasNext()) {
            DirNodeDto next = iterator.next();
            if (request.getDelId().equals(next.getId())) {
                iterator.remove();
                break;
            }
        }
        bizMapper.updateContent(request.getProductLineId(), JSONObject.toJSONString(root), request.getChannel());
        return root;
    }

    @Override
    public DirNodeDto getDir(String bizId, DirNodeDto root) {
        if (root == null) {
            return null;
        }
        if (bizId.equals(root.getId())) {
            return root;
        }

        List<DirNodeDto> children = root.getChildren();
        for (DirNodeDto child : children) {
            DirNodeDto dir = getDir(bizId, child);
            if (dir != null) {
                return dir;
            }
        }
        return null;
    }

    @Override
    public DirNodeDto getDirTree(Long productLineId, Integer channel) {
        Biz dbBiz = bizMapper.selectOne(productLineId, channel);
        // 如果有，那么就直接返回
        if (dbBiz != null) {
            return JSONObject.parseObject(dbBiz.getContent(), DirNodeDto.class);
        }

        // 如果没有，则会自动生成一个
        DirNodeDto root = new DirNodeDto();
        root.setId("root");
        root.setText("主文件夹");

        Set<String> ids = caseMapper.findCaseIdsInBiz(productLineId, channel);

        DirNodeDto child = new DirNodeDto();
        child.setId("-1");
        child.setParentId(root.getId());
        child.setText("未分类用例集");
        child.setCaseIds(ids);
        root.getChildren().add(child);

        Biz biz = new Biz();
        biz.setProductLineId(productLineId);
        biz.setChannel(channel);
        biz.setContent(JSONObject.toJSONString(root));

        bizMapper.insert(biz);
        root.getCaseIds().addAll(child.getCaseIds());
        return root;
    }

    @Override
    public DirNodeDto getProjectTree(Long projectId, Long productLineId, Integer channel) {
//        Biz dbBiz = bizMapper.selectOne(productLineId, channel);
        DirNodeDto root = new DirNodeDto();
        root.setId("root");

        DirNodeDto child = new DirNodeDto();
        Set<String> ids = caseMapper.findCaseIdsInProjectId(projectId, productLineId, channel);
        // 如果有，那么就直接返回
        if (ids != null) {
            // 如果没有，则会自动生成一个
            child.setId("-1");
            child.setParentId(root.getId());
            child.setText("未分类用例集");
            child.setCaseIds(ids);
            root.getChildren().add(child);
        }

        root.getCaseIds().add(String.valueOf(child.getCaseIds()));
        return root;
    }

    @Override
    public DirTreeResp getAllCaseDir(DirNodeDto root) {
        DirTreeResp resp = new DirTreeResp();
        addChildrenCaseIds(root);
        resp.getChildren().add(root);
        return resp;
    }

    @Override
    public List<Long> getCaseIds(Long productLineId, String bizId, Integer channel) {
//        DirTreeResp resp = getAllCaseDir(getDirTree(productLineId, channel));
//        DirNodeDto dir = getDir(bizId, resp.getChildren().get(0));
        Set<String> caseIds = caseMapper.findCaseIdsInBiz(productLineId, channel);
        return caseIds.stream().map(Long::valueOf).collect(Collectors.toList());
    }

    @Override
    public List<Long> getCaseIdLists(Long projectId,  Long productLineId, String bizId, Integer channel) {
        DirTreeResp resp = getAllCaseDir(getProjectTree(projectId, productLineId, channel));
        LOGGER.info("resp======" + resp);
        DirNodeDto dir = getDir(bizId, resp.getChildren().get(0));
        Set<String> caseIds = dir.getCaseIds();
        return caseIds.stream().map(Long::valueOf).collect(Collectors.toList());
    }

    /**
     * 将子目录的所有caseId分配到父目录
     *
     * @param root 当前节点
     */
    private void addChildrenCaseIds(DirNodeDto root){
        if (root == null) {
            return;
        }
        for (DirNodeDto child : root.getChildren()){
            addChildrenCaseIds(child);
            root.getCaseIds().addAll(child.getCaseIds());
        }
    }

    /**
     *  校验同级节点下是否存在相同名字的子节点
     *
     * @param text  节点名称
     * @param parentId  父节点id
     * @param dirNodeDto  节点内容
     */
    private void checkNodeExists(final String text, final String parentId, final DirNodeDto dirNodeDto) {
        if (parentId.equalsIgnoreCase(dirNodeDto.getId())) {
            List<DirNodeDto> childrenNodes = dirNodeDto.getChildren();
            if (childrenNodes.stream().anyMatch(node -> text.equalsIgnoreCase(node.getText()))) {
                throw new CaseServerException("目标节点已存在", StatusCode.NODE_ALREADY_EXISTS);
            }
        }
        List<DirNodeDto> childrenNodes = dirNodeDto.getChildren();
        childrenNodes.forEach(node -> checkNodeExists(text, parentId, node));
    }

    /**
     *  获取当前节点的父节点id
     * @param nodeId ： 节点id
     * @param dirTree： 节点内容
     * @return 返回父节点id或者null
     */
    private String getParentIdWithRecursion(final String nodeId, final DirNodeDto dirTree) {
        if (nodeId.equalsIgnoreCase(dirTree.getId())) {
            return dirTree.getParentId();
        }
        List<DirNodeDto> children = dirTree.getChildren();
        for (DirNodeDto node : children) {
            String parentId = getParentIdWithRecursion(nodeId, node);
            if (parentId != null) {
                return parentId;
            }
        }
        return null;
    }
}
