package me.xiaokui.modules.system.service.mapstruct;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import me.xiaokui.modules.system.domain.Project;
import me.xiaokui.modules.system.service.dto.ProjectDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-04-22T15:32:12+0800",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public Project toEntity(ProjectDto dto) {
        if ( dto == null ) {
            return null;
        }

        Project project = new Project();

        project.setCreateBy( dto.getCreateBy() );
        project.setUpdatedBy( dto.getUpdatedBy() );
        project.setCreateTime( dto.getCreateTime() );
        project.setUpdateTime( dto.getUpdateTime() );
        project.setId( dto.getId() );
        project.setPid( dto.getPid() );
        project.setSubCount( dto.getSubCount() );
        project.setName( dto.getName() );
        project.setProjectSort( dto.getProjectSort() );
        project.setEnabled( dto.getEnabled() );

        return project;
    }

    @Override
    public ProjectDto toDto(Project entity) {
        if ( entity == null ) {
            return null;
        }

        ProjectDto projectDto = new ProjectDto();

        projectDto.setCreateBy( entity.getCreateBy() );
        projectDto.setUpdatedBy( entity.getUpdatedBy() );
        projectDto.setCreateTime( entity.getCreateTime() );
        projectDto.setUpdateTime( entity.getUpdateTime() );
        projectDto.setId( entity.getId() );
        projectDto.setName( entity.getName() );
        projectDto.setEnabled( entity.getEnabled() );
        projectDto.setProjectSort( entity.getProjectSort() );
        projectDto.setPid( entity.getPid() );
        projectDto.setSubCount( entity.getSubCount() );

        return projectDto;
    }

    @Override
    public List<Project> toEntity(List<ProjectDto> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Project> list = new ArrayList<Project>( dtoList.size() );
        for ( ProjectDto projectDto : dtoList ) {
            list.add( toEntity( projectDto ) );
        }

        return list;
    }

    @Override
    public List<ProjectDto> toDto(List<Project> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<ProjectDto> list = new ArrayList<ProjectDto>( entityList.size() );
        for ( Project project : entityList ) {
            list.add( toDto( project ) );
        }

        return list;
    }
}
