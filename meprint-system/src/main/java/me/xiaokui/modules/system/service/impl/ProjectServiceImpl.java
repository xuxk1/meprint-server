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
package me.xiaokui.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.xiaokui.exception.BadRequestException;
import me.xiaokui.modules.system.domain.Project;
import me.xiaokui.modules.system.domain.User;
import me.xiaokui.modules.system.repository.ProjectRepository;
import me.xiaokui.modules.system.repository.RoleRepository;
import me.xiaokui.modules.system.repository.UserRepository;
import me.xiaokui.modules.system.service.ProjectService;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import me.xiaokui.modules.system.service.dto.ProjectQueryCriteria;
import me.xiaokui.modules.system.service.mapstruct.ProjectMapper;
import me.xiaokui.utils.FileUtil;
import me.xiaokui.utils.enums.DataScopeEnum;
import me.xiaokui.utils.*;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "project")
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final me.xiaokui.utils.RedisUtils redisUtils;
    private final RoleRepository roleRepository;

    @Override
    public List<ProjectDto> queryAll(ProjectQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = new Sort(Sort.Direction.ASC, "projectSort");
        String dataScopeType = me.xiaokui.utils.SecurityUtils.getDataScopeType();
        if (isQuery) {
            if(dataScopeType.equals(DataScopeEnum.ALL.getValue())){
                criteria.setPidIsNull(true);
            }
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(), new ArrayList<>());
            List<String> fieldNames = new ArrayList<String>(){{ add("pidIsNull");add("enabled");}};
            for (Field field : fields) {
                //???????????????????????????????????????private??????????????????
                field.setAccessible(true);
                Object val = field.get(criteria);
                if(fieldNames.contains(field.getName())){
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        List<ProjectDto> list = projectMapper.toDto(projectRepository.findAll((root, criteriaQuery, criteriaBuilder) -> me.xiaokui.utils.QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if(StringUtils.isBlank(dataScopeType)){
            return deduplication(list);
        }
        return list;
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public ProjectDto findById(Long id) {
        Project project = projectRepository.findById(id).orElseGet(Project::new);
        ValidationUtil.isNull(project.getId(),"Project","id",id);
        return projectMapper.toDto(project);
    }

    @Override
    public List<Project> findByPid(long pid) {
        return projectRepository.findByPid(pid);
    }

    @Override
    public Set<Project> findByRoleId(Long id) {
        return projectRepository.findByRoleId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Project resources) {
        projectRepository.save(resources);
        // ?????????????????????
        resources.setSubCount(0);
        // ????????????
        updateSubCnt(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Project resources) {
        // ????????????
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new me.xiaokui.exception.BadRequestException("?????????????????????");
        }
        Project project = projectRepository.findById(resources.getId()).orElseGet(Project::new);
        me.xiaokui.utils.ValidationUtil.isNull( project.getId(),"Project","id",resources.getId());
        resources.setId(project.getId());
        projectRepository.save(resources);
        // ?????????????????????????????????
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        // ????????????
        delCaches(resources.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<ProjectDto> projectDtos) {
        for (ProjectDto projectDto : projectDtos) {
            // ????????????
            delCaches(projectDto.getId());
            projectRepository.deleteById(projectDto.getId());
            updateSubCnt(projectDto.getPid());
        }
    }

    @Override
    public void download(List<ProjectDto> projectDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ProjectDto projectDto : projectDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("????????????", projectDto.getName());
            map.put("????????????", projectDto.getEnabled() ? "??????" : "??????");
            map.put("????????????", projectDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Set<ProjectDto> getDeleteProjects(List<Project> menuList, Set<ProjectDto> projectDtos) {
        for (Project project : menuList) {
            projectDtos.add(projectMapper.toDto(project));
            List<Project> projects = projectRepository.findByPid(project.getId());
            if(projects!=null && projects.size()!=0){
                getDeleteProjects(projects, projectDtos);
            }
        }
        return projectDtos;
    }

    @Override
    public List<Long> getProjectChildren(List<Project> projectList) {
        List<Long> list = new ArrayList<>();
        projectList.forEach(project -> {
                    if (project!=null && project.getEnabled()) {
                        List<Project> projects = projectRepository.findByPid(project.getId());
                        if (projectList.size() != 0) {
                            list.addAll(getProjectChildren(projects));
                        }
                        list.add(project.getId());
                    }
                }
        );
        return list;
    }

    @Override
    public List<ProjectDto> getSuperior(ProjectDto projectDto, List<Project> projects) {
        if(projectDto.getPid() == null){
            projects.addAll(projectRepository.findByPidIsNull());
            return projectMapper.toDto(projects);
        }
        projects.addAll(projectRepository.findByPid(projectDto.getPid()));
        return getSuperior(findById(projectDto.getPid()), projects);
    }

    @Override
    public Object buildTree(List<ProjectDto> projectDtos) {
        Set<ProjectDto> trees = new LinkedHashSet<>();
        Set<ProjectDto> depts= new LinkedHashSet<>();
        List<String> deptNames = projectDtos.stream().map(ProjectDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (ProjectDto projectDto : projectDtos) {
            isChild = false;
            if (projectDto.getPid() == null) {
                trees.add(projectDto);
            }
            for (ProjectDto it : projectDtos) {
                if (it.getPid() != null && projectDto.getId().equals(it.getPid())) {
                    isChild = true;
                    if (projectDto.getChildren() == null) {
                        projectDto.setChildren(new ArrayList<>());
                    }
                    projectDto.getChildren().add(it);
                }
            }
            if(isChild) {
                depts.add(projectDto);
            } else if(projectDto.getPid() != null &&  !deptNames.contains(findById(projectDto.getPid()).getName())) {
                depts.add(projectDto);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String,Object> map = new HashMap<>(2);
        map.put("totalElements",projectDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? projectDtos :trees);
        return map;
    }

    @Override
    public void verification(Set<ProjectDto> projectDtos) {
        Set<Long> projectIds = projectDtos.stream().map(ProjectDto::getId).collect(Collectors.toSet());
        if(userRepository.countByDepts(projectIds) > 0){
            throw new me.xiaokui.exception.BadRequestException("??????????????????????????????????????????????????????");
        }
        if(roleRepository.countByDepts(projectIds) > 0){
            throw new BadRequestException("??????????????????????????????????????????????????????");
        }
    }

    private void updateSubCnt(Long deptId){
        if(deptId != null){
            int count = projectRepository.countByPid(deptId);
            projectRepository.updateSubCntById(count, deptId);
        }
    }

    private List<ProjectDto> deduplication(List<ProjectDto> list) {
        List<ProjectDto> projectDtos = new ArrayList<>();
        for (ProjectDto projectDto : list) {
            boolean flag = true;
            for (ProjectDto dto : list) {
                if (dto.getId().equals(projectDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                projectDtos.add(projectDto);
            }
        }
        return projectDtos;
    }

    /**
     * ????????????
     * @param id /
     */
    public void delCaches(Long id){
        List<User> users = userRepository.findByDeptRoleId(id);
        // ??????????????????
        redisUtils.delByKeys("data::user:",users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del("project::id:" + id);
    }
}
