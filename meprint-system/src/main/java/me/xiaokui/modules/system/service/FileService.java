package me.xiaokui.modules.system.service;

import me.xiaokui.modules.system.domain.request.cases.FileImportReq;
import me.xiaokui.modules.system.domain.response.cases.ExportXmindResp;

import javax.annotation.Resource;

/**
 * 文件上传与导出服务接口
 *
 * @author didi
 * @date 2020/10/22
 */
@Resource
public interface FileService {

    /**
     * 导入x-mind文件生成case
     *
     * @param req 请求体
     * @return 生成的case-id
     * @throws Exception 任何可能的异常
     */
    Long importXmindFile(FileImportReq req) throws Exception;

    /**
     * 导出xmind内容
     *
     * @param id 用例id
     * @param userAgent http请求头表示来源
     * @return 响应体
     * @throws Exception 任何可能的异常
     */
    ExportXmindResp exportXmindFile(Long id, String userAgent) throws Exception;
}
