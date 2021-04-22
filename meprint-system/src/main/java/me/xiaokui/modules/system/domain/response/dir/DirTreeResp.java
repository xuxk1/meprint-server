package me.xiaokui.modules.system.domain.response.dir;

import lombok.Data;
import me.xiaokui.modules.system.service.dto.DirNodeDto;
import me.xiaokui.modules.system.service.dto.ProjectDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 树结构
 *
 * @author hcy
 * @date 2020/11/11
 */
@Data
public class DirTreeResp {

    private List<DirNodeDto> children = new ArrayList<>();
    private List<ProjectDto> childrens = new ArrayList<>();
}
