package me.xiaokui.modules.system.service.mapstruct;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import me.xiaokui.modules.system.domain.Job;
import me.xiaokui.modules.system.service.dto.JobDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-04-22T15:32:12+0800",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
@Component
public class JobMapperImpl implements JobMapper {

    @Override
    public Job toEntity(JobDto dto) {
        if ( dto == null ) {
            return null;
        }

        Job job = new Job();

        job.setCreateBy( dto.getCreateBy() );
        job.setUpdatedBy( dto.getUpdatedBy() );
        job.setCreateTime( dto.getCreateTime() );
        job.setUpdateTime( dto.getUpdateTime() );
        job.setId( dto.getId() );
        job.setName( dto.getName() );
        if ( dto.getJobSort() != null ) {
            job.setJobSort( dto.getJobSort().longValue() );
        }
        job.setEnabled( dto.getEnabled() );

        return job;
    }

    @Override
    public JobDto toDto(Job entity) {
        if ( entity == null ) {
            return null;
        }

        JobDto jobDto = new JobDto();

        jobDto.setCreateBy( entity.getCreateBy() );
        jobDto.setUpdatedBy( entity.getUpdatedBy() );
        jobDto.setCreateTime( entity.getCreateTime() );
        jobDto.setUpdateTime( entity.getUpdateTime() );
        jobDto.setId( entity.getId() );
        if ( entity.getJobSort() != null ) {
            jobDto.setJobSort( entity.getJobSort().intValue() );
        }
        jobDto.setName( entity.getName() );
        jobDto.setEnabled( entity.getEnabled() );

        return jobDto;
    }

    @Override
    public List<Job> toEntity(List<JobDto> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Job> list = new ArrayList<Job>( dtoList.size() );
        for ( JobDto jobDto : dtoList ) {
            list.add( toEntity( jobDto ) );
        }

        return list;
    }

    @Override
    public List<JobDto> toDto(List<Job> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<JobDto> list = new ArrayList<JobDto>( entityList.size() );
        for ( Job job : entityList ) {
            list.add( toDto( job ) );
        }

        return list;
    }
}
