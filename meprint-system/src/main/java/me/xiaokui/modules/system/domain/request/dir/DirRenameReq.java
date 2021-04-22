package me.xiaokui.modules.system.domain.request.dir;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.xiaokui.constants.BizConstant;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import org.springframework.util.StringUtils;

import static me.xiaokui.constants.BizConstant.UNSORTED_BIZ_ID;

/**
 * 文件夹 重命名
 *
 * @author didi
 * @date 2020/9/11
 */
@Data
@AllArgsConstructor
public class DirRenameReq implements ParamValidate {

    private String id;

    private Long productLineId;

    private String text;

    private Integer channel;

    @Override
    public void validate() {
        if (BizConstant.UNSORTED_BIZ_ID.equals(id) || StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("请选择正确的节点进行重命名");
        }
        if (productLineId == null || productLineId <= 0) {
            throw new IllegalArgumentException("业务线id为空或者非法");
        }
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException("文件夹名称不能为空");
        }
        if (channel == null) {
            throw new IllegalArgumentException("渠道为空");
        }
    }
}
