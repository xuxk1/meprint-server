package me.xiaokui.modules.system.service.dto;

import lombok.Data;
import me.xiaokui.modules.system.rest.CaseController;

import java.util.List;

/**
 * 圈选用例所需要用到的结构体
 *
 * @author hcy
 * @date 2020/10/28
 * @see CaseController#getCountByCondition(Long, String[], String[])
 */
@Data
public class PickCaseDto {

    /**
     * 优先级 ["0", "1", "2"....]
     */
    private List<String> priority;

    /**
     * 资源 ["用户自己", "在测试用例中", "定义的标签"]
     */
    private List<String> resource;
}
