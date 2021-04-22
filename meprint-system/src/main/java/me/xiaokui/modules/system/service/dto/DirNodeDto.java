package me.xiaokui.modules.system.service.dto;


import lombok.Data;
import me.xiaokui.modules.system.service.impl.RecordServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文件夹节点转换体
 *
 * @author didi
 * @date 2020/10/28
 * @see RecordServiceImpl#(me.xiaokui.modules.system.service.dto.MergeCaseDto)
 */
@Data
public class DirNodeDto {

    private String id;
    private String text;
    private String parentId;
    private Set<String> caseIds = new HashSet<>();

    private List<DirNodeDto> children = new ArrayList<>();
}
