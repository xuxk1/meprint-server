package me.xiaokui.modules.system.service;

import me.xiaokui.modules.system.domain.request.dir.DirCreateReq;
import me.xiaokui.modules.system.domain.request.dir.DirDeleteReq;
import me.xiaokui.modules.system.domain.request.dir.DirRenameReq;
import me.xiaokui.modules.system.domain.response.dir.DirTreeResp;
import me.xiaokui.modules.system.service.dto.DirNodeDto;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文件夹接口
 *
 * @author didi
 * @date 2020/9/9
 */
@Resource
@Service
public interface DirService {

    /**
     * 增加文件夹
     *
     * @param request 请求体
     * @return 树
     */
    DirNodeDto addDir(DirCreateReq request);

    /**
     * 重命名文件夹
     *
     * @param request 请求体
     * @return 树
     */
    DirNodeDto renameDir(DirRenameReq request);

    /**
     * 删除文件夹
     *
     * @param request 请求体
     * @return 树
     */
    DirNodeDto delDir(DirDeleteReq request);

    /**
     * 获取一个节点的内容
     *
     * @param bizId 节点id
     * @param root 要搜索的树
     * @return 树
     */
    DirNodeDto getDir(String bizId, DirNodeDto root);

    /**
     * 查询文件树
     *
     * @param productLineId 业务线id
     * @param channel 渠道 默认为1
     * @return 树
     */
    DirNodeDto getDirTree(Long productLineId, Integer channel);

    /**
     * 查询文件树
     * @param productLineId 业务线id
     * @param projectId 项目id
     * @param channel 渠道 默认为1
     * @return 树
     */
    DirNodeDto getProjectTree(Long projectId, Long productLineId, Integer channel);

    /**
     * 查询文件树，并且给文件树装配caseIds
     *
     * @param root 通过getDirTree传入的整棵树
     * @return 响应体
     */
    DirTreeResp getAllCaseDir(DirNodeDto root);

    /**
     * 获取当前节点的关联用例
     *
     * @param productLineId 业务线id
     * @param bizId 文件夹id
     * @param channel 渠道
     * @return case-id-list
     */
    List<Long> getCaseIds(Long productLineId, String bizId, Integer channel);

    /**
     * 获取当前节点的关联用例
     *
     * @param productLineId 业务线id
     * @param projectId 项目id
     * @param channel 渠道
     * @return case-id-list
     */
    List<Long> getCaseIdLists(Long projectId, Long productLineId, String bizId, Integer channel);
}
