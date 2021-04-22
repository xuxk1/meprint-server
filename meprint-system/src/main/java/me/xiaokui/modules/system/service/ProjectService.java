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
package me.xiaokui.modules.system.service;

import me.xiaokui.modules.system.domain.Project;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import me.xiaokui.modules.system.service.dto.ProjectQueryCriteria;
import me.xiaokui.modules.system.domain.Dept;
import me.xiaokui.modules.system.domain.Project;
import me.xiaokui.modules.system.service.dto.DeptDto;
import me.xiaokui.modules.system.service.dto.DeptQueryCriteria;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import me.xiaokui.modules.system.service.dto.ProjectQueryCriteria;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
* @author Xiaokui
* @date 2021-03-25
*/
public interface ProjectService {

    /**
     * 查询所有数据
     * @param criteria 条件
     * @param isQuery /
     * @throws Exception /
     * @return /
     */
    List<ProjectDto> queryAll(ProjectQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    ProjectDto findById(Long id);

    /**
     * 创建
     * @param resources /
     */
    void create(Project resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(Project resources);

    /**
     * 删除
     * @param projectDtos /
     *
     */
    void delete(Set<ProjectDto> projectDtos);

    /**
     * 根据PID查询
     * @param pid /
     * @return /
     */
    List<Project> findByPid(long pid);

    /**
     * 根据角色ID查询
     * @param id /
     * @return /
     */
    Set<Project> findByRoleId(Long id);

    /**
     * 导出数据
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<ProjectDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 获取待删除的项目
     * @param projectList /
     * @param projectDtos /
     * @return /
     */
    Set<ProjectDto> getDeleteProjects(List<Project> projectList, Set<ProjectDto> projectDtos);

    /**
     * 根据ID获取同级与上级数据
     * @param projectDto /
     * @param projects /
     * @return /
     */
    List<ProjectDto> getSuperior(ProjectDto projectDto, List<Project> projects);

    /**
     * 构建树形数据
     * @param projectDtos /
     * @return /
     */
    Object buildTree(List<ProjectDto> projectDtos);

    /**
     * 获取
     * @param projectList
     * @return
     */
    List<Long> getProjectChildren(List<Project> projectList);

    /**
     * 验证是否被角色或用户关联
     * @param projectDtos /
     */
    void verification(Set<ProjectDto> projectDtos);
}
