package me.xiaokui.modules.system.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.xiaokui.modules.system.service.impl.RecordServiceImpl;

/**
 * 给getData专用的转换体
 *
 * @author hcy
 * @date 2020/10/28
 * @see RecordServiceImpl#getData(MergeCaseDto)
 */
@Data
@AllArgsConstructor
public class MergeCaseDto {

    private Long caseId;

    private String chooseContent;

    private String recordContent;

    private Integer env;

    private Long recordId;
}
