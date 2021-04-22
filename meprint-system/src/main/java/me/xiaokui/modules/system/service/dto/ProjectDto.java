/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.xiaokui.modules.system.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import me.xiaokui.base.BaseDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@Getter
@Setter
public class ProjectDto extends BaseDTO implements Serializable {

    private Long id;

    private String name;

    private Boolean enabled;

    private Integer projectSort;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ProjectDto> children;

    private Long pid;

    private Integer subCount;

    public Boolean getHasChildren() {
        return subCount > 0;
    }

    public Boolean getLeaf() {
        return subCount <= 0;
    }

    public String getLabel() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectDto projectDto = (ProjectDto) o;
        return Objects.equals(id, projectDto.id) &&
                Objects.equals(name, projectDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
