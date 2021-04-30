package me.xiaokui.modules.system.domain.request.cases;

import lombok.Data;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import org.springframework.util.StringUtils;

/**
 * 用例 筛选与查询
 *
 * @author hcy
 * @date 2020/8/12
 */
@Data
public class CaseQueryReq implements ParamValidate {

    private Long id;

    private Integer caseType;

    private Long lineId;

    private String title;

    private String creator;

    private String requirementId;

    private String beginTime;

    private String endTime;

    private Integer channel;

    private String bizId;

    private Long projectId;

    private Integer pageNum;

    private Integer pageSize;

    public CaseQueryReq(Integer caseType, String title, String creator, String reqIds, String beginTime, String endTime, Integer channel, String bizId, Long lineId, Integer pageNum, Integer pageSize, Long projectId) {
        this.caseType = caseType;
        this.title = title;
        this.creator = creator;
        this.requirementId = reqIds;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.channel = channel;
        this.bizId = bizId;
        this.lineId = lineId;
        this.projectId = projectId;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    @Override
    public void validate() {

        if (channel == null || channel < 0) {
            throw new IllegalArgumentException("渠道为空或者非法");
        }
        if (lineId == null || lineId < 0L) {
            throw new IllegalArgumentException("业务线id为空或者非法");
        }
        if (StringUtils.isEmpty(bizId)) {
            // 特殊点，没有传bizId就给-1，而不是报错
            throw new IllegalArgumentException("文件夹id");
        }
        if (caseType == null || caseType < 0) {
            throw new IllegalArgumentException("用例类型为空或者非法");
        }
        if (StringUtils.isEmpty(title)) {
            throw new IllegalArgumentException("标题为空");
        }
        if (StringUtils.isEmpty(creator)) {
            throw new IllegalArgumentException("创建人为空");
        }
        if (projectId == null || projectId < 0L) {
            throw new IllegalArgumentException("项目id为空或者非法");
        }
    }
}
