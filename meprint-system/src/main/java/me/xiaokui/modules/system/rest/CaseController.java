package me.xiaokui.modules.system.rest;

import com.alibaba.fastjson.support.spring.annotation.ResponseJSONP;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.xiaokui.annotation.Log;
import me.xiaokui.annotation.rest.AnonymousGetMapping;
import me.xiaokui.exception.BadRequestException;
import me.xiaokui.modules.mapper.BizMapper;
import me.xiaokui.modules.system.domain.request.cases.*;
import me.xiaokui.modules.system.domain.request.ws.WsSaveReq;
import me.xiaokui.modules.system.domain.response.controller.Response;
import me.xiaokui.modules.system.service.CaseService;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.mapper.BizMapper;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.system.domain.request.cases.*;
import me.xiaokui.modules.system.domain.request.ws.WsSaveReq;
import me.xiaokui.modules.system.service.CaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * 用例相关接口
 *
 * @author didi
 * @date 2020/11/20
 */
@Api(tags = "用例：用例管理")
@RestController
@RequestMapping("/api/case")
@RequiredArgsConstructor
public class CaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseController.class);
    private static BadRequestException bRequestException;

    @Autowired
    CaseService caseService;

    @Autowired
    private BizMapper bizMapper;

    /**
     * 用例 - 根据文件夹id获取所有用例
     *
     * @param productLineId 业务线id
     * @param bizId 用例id
     * @param title 用例标题
     * @param creator 创建人前缀
     * @param requirementId 需求id
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param channel 1
     * @param pageNum 页码
     * @param pageSize 页面承载量
     * @return 分页接口
     */
    @ApiOperation("用例列表")
    @GetMapping(value = "/list")
    @PreAuthorize("@el.check('case:list')")
    public Response<?> getCaseList(@RequestParam Integer channel, Long productLineId, String bizId,
                                              Long projectId, String title, String creator, String requirementId,
                                              String beginTime, String endTime, Integer pageNum, Integer pageSize) {
        try {
            CaseQueryReq caseQueryReq = new CaseQueryReq(0, title, creator, requirementId, beginTime,
                    endTime, channel, bizId, productLineId, pageNum, pageSize, projectId);
            return Response.success(caseService.getCaseList(
                    caseQueryReq));
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case list]view list case failed. params={}, e={} ", channel, productLineId, bizId, e.getMessage());
//            return Response.build(StatusCode.SERVER_BUSY_ERROR);
            return Response.success(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 列表 - 创建或者复制用例
     *
     * @param request 请求体
     * @return 响应体
     */
    @ApiOperation("创建用例")
    @PostMapping(value = "/create")
    @PreAuthorize("@el.check('case:create')")
    public ResponseEntity<Object> createOrCopyCase(@RequestBody CaseCreateReq request) {
        request.validate();
        try {
            return new ResponseEntity<>(caseService.insertOrDuplicateCase(request),HttpStatus.OK);
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case Create]Create or duplicate test case failed. params={}, e={} ", request.toString(), e.getMessage());
//            return Response.build(StatusCode.SERVER_BUSY_ERROR);
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 列表 - 修改用例属性
     *
     * @param request 请求体
     * @return 响应体
     */
    @ApiOperation("编辑用例")
    @PostMapping(value = "/edit")
    @PreAuthorize("@el.check('case:edit')")
    public ResponseEntity<Object> editCase(@RequestBody CaseEditReq request) {
        request.validate();
        try {
            return new ResponseEntity<>(caseService.updateCase(request), HttpStatus.OK);
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case Update]Update test case failed. params={} e={} ", request.toString(), e.getMessage());
//            return Response.build(StatusCode.SERVER_BUSY_ERROR);
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 列表 - 删除用例
     *
     * @param request 请求体
     * @return 响应体
     */
    @ApiOperation("删除用例")
    @PostMapping(value = "/delete")
    @PreAuthorize("@el.check('case:delete')")
    public ResponseEntity<Object> deleteCase(@RequestBody CaseDeleteReq request) {
        LOGGER.info("caseId=====" + request);
        request.validate();
        try {
            return new ResponseEntity<>(caseService.deleteCase(request.getId()), HttpStatus.OK);
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            LOGGER.error("[Case Delete]Delete test case failed. params={} e={} ", request.toString(), e.getMessage());
            e.printStackTrace();
//            return Response.build(StatusCode.SERVER_BUSY_ERROR);
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 列表 - 查看用例详情
     *
     * @param caseId 用例id
     * @return 响应体
     */
    @Log("查看用例详情")
    @ApiOperation("查看用例详情")
    @GetMapping(value = "/detail")
    public ResponseEntity<Object> getCaseDetail(@RequestParam @NotNull(message = "用例id为空") Long caseId) {
        try {
            return new ResponseEntity<>(caseService.getCaseDetail(caseId), HttpStatus.OK);
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case detail]View detail of test case failed. params={}, e={} ", caseId, e.getMessage());
//            return Response.build(StatusCode.SERVER_BUSY_ERROR);
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 配合list 筛选时获取所有创建人的列表
     *
     * @param caseType 用例类型
     * @param productLineId 业务线id
     * @return 响应体
     */
    @Log("筛选时获取所有创建人的列表")
    @ApiOperation("筛选时获取所有创建人的列表")
    @GetMapping(value = "/listCreators")
    public ResponseEntity<Object> listCreators(@RequestParam @NotNull(message = "用例类型为空") Integer caseType,
                                               @RequestParam @NotNull(message = "业务线为空") Long productLineId) {
//        return Response.success(caseService.listCreators(caseType, productLineId));
        try {
            return new ResponseEntity<>(caseService.listCreators(caseType,productLineId), HttpStatus.OK);
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case listCreators]View listCreators of test case failed. params={}, e={} ", caseType, e.getMessage());
//            return Response.build(StatusCode.SERVER_BUSY_ERROR);
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 配合detail 修改圈选用例时统计的用例条目数据
     *
     * @param caseId 用例id
     * @param priority 优先级列表
     * @param resource 资源列表
     * @return 响应体
     */
    @Log("修改圈选用例时统计的用例条目数据")
    @ApiOperation("修改圈选用例时统计的用例条目数据")
    @GetMapping(value = "/countByCondition")
    public Response<?> getCountByCondition(@RequestParam Long caseId, String[] priority, String[] resource) {
        CaseConditionReq req = new CaseConditionReq(caseId, priority, resource);
        req.validate();
        return Response.success(caseService.getCountByCondition(req));
    }

    /**
     * 脑图 - 获取上方用例概览信息
     *
     * @param id 用例id
     * @return 概览信息
     */
    @Log("获取上方用例概览信息")
    @ApiOperation("获取上方用例概览信息")
    @GetMapping(value = "/getCaseInfo")
    public Response<?> getCaseGeneralInfo(@RequestParam @NotNull(message = "用例id为空") Long id) {
//        return new ResponseEntity<>(caseService.getCaseGeneralInfo(id), HttpStatus.OK);
        return Response.success(caseService.getCaseGeneralInfo(id));
    }

    /**
     * 脑图 - 保存按钮 可能是case也可能是record
     *
     * @param req 请求体
     * @return 响应体
     */
    @Log("用例更新")
    @ApiOperation("用例更新")
    @PreAuthorize("@el.check('case:update')")
    @PostMapping(value = "/update")
    public Response<?> updateWsCase(@RequestBody WsSaveReq req) {
        try {
            caseService.wsSave(req);
            return Response.success();
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case Update]Update test case failed. params={} e={} ", req.toString(), e.getMessage());
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

}
