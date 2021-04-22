package me.xiaokui.modules.system.service.mapstruct;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import me.xiaokui.modules.system.domain.Project;
import me.xiaokui.modules.system.service.dto.ProjectSmallDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-04-22T15:32:11+0800",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
@Component
public class ProjectSmallMapperImpl implements ProjectSmallMapper {

    @Override
    public Project toEntity(ProjectSmallDto dto) {
        if ( dto == null ) {
            return null;
        }

        Project project = new Project();

        project.setId( dto.getId() );
        project.setName( dto.getName() );

        return project;
    }

    @Override
    public ProjectSmallDto toDto(Project entity) {
        if ( entity == null ) {
            return null;
        }

        ProjectSmallDto projectSmallDto = new ProjectSmallDto();

        projectSmallDto.setId( entity.getId() );
        projectSmallDto.setName( entity.getName() );

        return projectSmallDto;
    }

    @Override
    public List<Project> toEntity(List<ProjectSmallDto> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Project> list = new ArrayList<Project>( dtoList.size() );
        for ( ProjectSmallDto projectSmallDto : dtoList ) {
            list.add( toEntity( projectSmallDto ) );
        }

        return list;
    }

    @Override
    public List<ProjectSmallDto> toDto(List<Project> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<ProjectSmallDto> list = new ArrayList<ProjectSmallDto>( entityList.size() );
        for ( Project project : entityList ) {
            list.add( toDto( project ) );
        }

        return list;
    }
}
