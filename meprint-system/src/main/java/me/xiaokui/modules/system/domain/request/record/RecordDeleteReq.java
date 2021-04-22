package me.xiaokui.modules.system.domain.request.record;

import lombok.Data;
import me.xiaokui.modules.system.domain.request.ParamValidate;
import me.xiaokui.modules.system.domain.request.ParamValidate;

/**
 * 任务 删除
 *
 * @author hcy
 * @date 2020/10/28
 */
@Data
public class RecordDeleteReq implements ParamValidate {

    private Long id;

    @Override
    public void validate() {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("任务id为空或不正确");
        }
    }
}
