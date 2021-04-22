package me.xiaokui.modules.system.domain.response.cases;

import lombok.Data;
import me.xiaokui.modules.system.domain.response.dir.BizListResp;
import me.xiaokui.modules.system.domain.response.dir.BizListResp;

import java.util.List;

/**
 * 用例详情
 *
 * @author hcy
 * @date 2020/9/7
 */
@Data
public class CaseDetailResp {

    private Integer caseType;

    private String description;

    private Long id;

    private String modifier;

    private String requirementId;

    private String title;

    private Long productLineId;

    private List<BizListResp> biz;

    private Long groupId;

    private Long projectId;

}
