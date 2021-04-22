package me.xiaokui.modules.system.domain.request.cases;

import lombok.Data;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import me.xiaokui.modules.system.domain.request.ParamValidate;

/**
 * 用例 逻辑删除
 *
 * @author hcy
 * @date 2020/9/7
 */
@Data
public class CaseDeleteReq implements ParamValidate {

    private Long id;

    @Override
    public void validate() {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用例id为空");
        }
    }
}
