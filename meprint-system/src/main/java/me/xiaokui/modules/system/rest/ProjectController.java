package me.xiaokui.modules.system.rest;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.xiaokui.annotation.Log;
import me.xiaokui.exception.BadRequestException;
import me.xiaokui.modules.system.domain.Project;
import me.xiaokui.modules.system.repository.ProjectRepository;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import me.xiaokui.modules.system.service.dto.ProjectQueryCriteria;
import me.xiaokui.utils.PageUtil;
import me.xiaokui.modules.system.domain.Project;
import me.xiaokui.modules.system.repository.ProjectRepository;
import me.xiaokui.modules.system.service.ProjectService;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import me.xiaokui.modules.system.service.dto.ProjectQueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @ClassName: ProjectController
 * @Description: 用例的项目管理
 * @Author xuxk
 * @Date 2021-04-09 18:02
 * @Memo 备注信息
 **/
@Api(tags = "用例：项目管理")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private static final String ENTITY_NAME = "project";
    private Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @ApiOperation("导出项目数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('project:list')")
    public void download(HttpServletResponse response, ProjectQueryCriteria criteria) throws Exception {
        projectService.download(projectService.queryAll(criteria, false), response);
    }

    @ApiOperation("查询项目")
    @GetMapping
    @PreAuthorize("@el.check('user:list','project:list')")
    public ResponseEntity<Object> query(ProjectQueryCriteria criteria) throws Exception {
        List<ProjectDto> projectDtos = projectService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(projectDtos, projectDtos.size()), HttpStatus.OK);
    }

    @ApiOperation("查询项目:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('user:list','project:list')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
        Set<ProjectDto> projectDtos  = new LinkedHashSet<>();
        for (Long id : ids) {
            ProjectDto projectDto = projectService.findById(id);
            List<ProjectDto> projects = projectService.getSuperior(projectDto, new ArrayList<>());
            projectDtos.addAll(projects);
        }
        return new ResponseEntity<>(projectService.buildTree(new ArrayList<>(projectDtos)),HttpStatus.OK);
    }

    @Log("新增项目")
    @ApiOperation("新增项目")
    @PostMapping
    @PreAuthorize("@el.check('project:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Project resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        projectService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改项目")
    @ApiOperation("修改项目")
    @PutMapping
    @PreAuthorize("@el.check('project:edit')")
    public ResponseEntity<Object> update(@Validated(Project.Update.class) @RequestBody Project resources){
        projectService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除项目")
    @ApiOperation("删除项目")
    @DeleteMapping
    @PreAuthorize("@el.check('project:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        logger.info("ids=====" + ids);
        Set<ProjectDto> projectDtos = new HashSet<>();
        for (Long id : ids) {
            List<Project> projectList = projectService.findByPid(id);
            logger.info("projectList=====" + projectList);
            projectDtos.add(projectService.findById(id));
            if(CollectionUtil.isNotEmpty(projectList)){
                projectDtos = projectService.getDeleteProjects(projectList, projectDtos);
            }
        }
        // 验证是否被角色或用户关联
        projectService.verification(projectDtos);
        projectService.delete(projectDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}


