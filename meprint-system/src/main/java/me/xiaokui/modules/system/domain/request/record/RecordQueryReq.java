package me.xiaokui.modules.system.domain.request.record;

import lombok.Data;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务 查询列表
 *
 * @author hcy
 * @date 2020/8/24
 */
@Data
public class RecordQueryReq implements ParamValidate {

    private Long caseId;

    private String title;

    private String owner;

    private String userName;

    private String requirementId;

    private String expectStartTime;

    private String expectEndTime;

    private Integer channel;

    private List<Long> reqIds;

    private Integer pageNum;

    private Integer pageSize;

    public RecordQueryReq(Integer channel, String[] reqIds, Integer pageNum, Integer pageSize) {
        // String[]转为List<Long>
        List<Long> reqIdList = new ArrayList<>();
        for (String reqId : reqIds) {
            reqIdList.add(Long.valueOf(reqId));
        }
        this.channel = channel;
        this.reqIds = reqIdList;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public RecordQueryReq(Long caseId, Integer pageNum, Integer pageSize, String userName) {
        this.caseId = caseId;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.userName = userName;
    }

    public RecordQueryReq(String title, String owner, String expectStartTime, String expectEndTime, Integer pageNum, Integer pageSize, String userName) {
        this.title = title;
        this.owner = owner;
        this.expectStartTime = expectStartTime;
        this.expectEndTime = expectEndTime;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.userName = userName;
    }

    @Override
    public void validate() {

        if (StringUtils.isEmpty(title)) {
            throw new IllegalArgumentException("标题为空");
        }

        if (StringUtils.isEmpty(owner)) {
            throw new IllegalArgumentException("负责人为空");
        }
    }
}
