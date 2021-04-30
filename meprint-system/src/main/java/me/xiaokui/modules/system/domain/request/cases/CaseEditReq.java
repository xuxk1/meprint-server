package me.xiaokui.modules.system.domain.request.cases;

import lombok.Data;
import me.xiaokui.constants.BizConstant;
import me.xiaokui.constants.SystemConstant;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * 用例 修改
 *
 * @author hcy
 * @date 2020/9/4
 */
@Data
public class CaseEditReq implements ParamValidate {

    /**
     * 必填 用例id
     */
    private Long id;

    /**
     * 必填 用例种类 默认给0
     */
    private Integer caseType;

    /**
     * 必填 用例标题
     */
    private String title;

    /**
     * 必填 修改人
     */
    private String modifier;

    /**
     * 必填 文件夹id列表 默认-1
     */
    private String bizId;

    /**
     * 必填 渠道 默认1
     */
    private Integer channel;

    /**
     * 非必填 需求id列表
     */
    private String requirementId;

    /**
     * 非必填 描述
     */
    private String description;

    /**
     * 非必填 用例id
     */
    private Long projectId;



    @Override
    public void validate() {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("要修改的用例id为空或者非法");
        }
        if (StringUtils.isEmpty(modifier)) {
            throw new IllegalArgumentException("请传入修改人");
        }
        // oe需要关联文件夹
        if (StringUtils.isEmpty(bizId)) {
            throw new IllegalArgumentException("文件夹选择为空");
        } else {
            long count = Arrays.stream(bizId.split(me.xiaokui.constants.SystemConstant.COMMA)).filter(BizConstant.ROOT_BIZ_ID::equals).count();
            if (count > 0) {
                throw new IllegalArgumentException("不可以在根文件夹下创建用例");
            }
        }
        if (StringUtils.isEmpty(title)) {
            throw new IllegalArgumentException("标题输入为空");
        }
        if (caseType == null) {
            throw new IllegalArgumentException("用例种类为空");
        }
        if (channel == null) {
            throw new IllegalArgumentException("渠道为空");
        }

        if (StringUtils.isEmpty(requirementId)) {
            requirementId = me.xiaokui.constants.SystemConstant.EMPTY_STR;
        }
        if (description == null) {
            description = SystemConstant.EMPTY_STR;
        }
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("要修改的项目ID为空或者非法");
        }
    }
}
