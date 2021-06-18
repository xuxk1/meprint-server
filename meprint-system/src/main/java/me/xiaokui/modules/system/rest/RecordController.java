package me.xiaokui.modules.system.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.xiaokui.annotation.Log;
import me.xiaokui.modules.system.domain.request.record.RecordQueryReq;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.util.enums.StatusCode;
import me.xiaokui.modules.util.exception.CaseServerException;
import me.xiaokui.modules.system.domain.request.record.RecordAddReq;
import me.xiaokui.modules.system.domain.request.record.RecordDeleteReq;
import me.xiaokui.modules.system.domain.request.record.RecordUpdateReq;
import me.xiaokui.modules.system.domain.request.ws.RecordWsClearReq;
import me.xiaokui.modules.system.domain.response.controller.Response;
import me.xiaokui.modules.system.service.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * 执行任务
 *
 * @author didi
 * @date 2020/11/20
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：任务管理")
@RequestMapping(value = "/api/record")
public class RecordController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);

    @Resource
    RecordService recordService;

    /**
     * 列表 - 根据用例下获取所有执行任务
     *
     * @param caseId 用例集id
     * @return 响应体
     */
    @ApiOperation("查询执行任务:根据ID获取所有执行任务")
    @GetMapping(value = "/list")
    @PreAuthorize("@el.check('user:list','task:list')")
    public Response<?> getRecordList(@RequestParam @NotNull(message = "用例id为空") Long caseId, Integer pageNum, Integer pageSize) {
        RecordQueryReq req = new RecordQueryReq(caseId, pageNum, pageSize);
        return Response.success(recordService.getListByCaseId(req));
    }

    /**
     * 列表 - 获取所有执行任务
     *
     * @return 响应体
     */
    @ApiOperation("查询所有执行任务")
    @PreAuthorize("@el.check('task:list')")
    @GetMapping(value = "/alllist")
    public Response<?> getRecordAllList(@RequestParam  String userName,
                                        @RequestParam(required = false)  String title,
                                        @RequestParam(required = false)  String owner,
                                        @RequestParam(required = false)  String expectStartTime,
                                        @RequestParam(required = false)  String expectEndTime,
                                        Integer pageNum, Integer pageSize) {
        RecordQueryReq req = new RecordQueryReq(title, owner, expectStartTime, expectEndTime, pageNum, pageSize, userName);
        return Response.success(recordService.getList(req));
    }

    /**
     * 列表 - 新增执行任务
     *
     * @param req 前端传参
     * @return 响应体
     */
    @Log("新增执行任务")
    @ApiOperation("新增执行任务")
    @PreAuthorize("@el.check('task:add')")
    @PostMapping(value = "/create")
    public Response<Long> createRecord(@RequestBody RecordAddReq req) {
        req.validate();
        try {
            return Response.success(recordService.addRecord(req));
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            LOGGER.error("[新增record出错]入参={}, 原因={}", req.toString(), e.getMessage());
            e.printStackTrace();
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 列表 - 修改执行任务
     *
     * @param req 请求体
     * @return 响应体
     */
    @Log("修改执行任务")
    @ApiOperation("修改执行任务")
    @PreAuthorize("@el.check('task:edit')")
    @PostMapping(value = "/edit")
    public Response<?> editRecord(@RequestBody RecordUpdateReq req) {
        req.validate();
        try {
            recordService.editRecord(req);
            return Response.success();
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            LOGGER.error("[列表页面-更新record属性出错]入参={}, 原因={}", req.toString(), e.getMessage());
            e.printStackTrace();
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 列表 - 删除执行任务
     *
     * @param req 请求体
     * @return 响应体
     */
    @Log("删除执行任务")
    @ApiOperation("删除执行任务")
    @PreAuthorize("@el.check('task:del')")
    @PostMapping(value = "/delete")
    public Response<?> deleteRecord(@RequestBody RecordDeleteReq req) {
        req.validate();
        try {
            recordService.delete(req.getId());
            return Response.success("删除成功");
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            LOGGER.error("[删除record错误]入参={}, 原因={}", req.toString(), e.getMessage());
            e.printStackTrace();
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 脑图 - 清理json中所有的执行记录
     *
     * @param req 请求体
     * @return 响应体
     */
    @Log("清除执行记录")
    @ApiOperation("清除执行记录")
    @PostMapping(value = "/clear")
    public Response<?> clearRecord(@RequestBody RecordWsClearReq req) {
        req.validate();
        try {
            return Response.success(recordService.wsClearRecord(req));
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            LOGGER.error("[协同页面-清除record执行记录出错]入参={}, 原因={}", req.toString(), e.getMessage());
            e.printStackTrace();
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 脑图 - 获取该任务用例上方的统计信息
     *
     * @param id 执行任务id
     * @return 响应体
     */
    @Log("获取任务详情")
    @ApiOperation("获取任务详情")
    @PreAuthorize("@el.check('task:run')")
    @GetMapping(value = "/getRecordInfo")
    public Response<?> getRecordGeneralInfo(@RequestParam @NotNull(message = "任务id为空") Long id) {
        return Response.success(recordService.getGeneralInfo(id));
    }

}
