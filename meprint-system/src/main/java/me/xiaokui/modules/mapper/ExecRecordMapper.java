package me.xiaokui.modules.mapper;

import me.xiaokui.modules.system.service.dto.RecordNumDto;
import me.xiaokui.modules.persistent.ExecRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 任务映射
 *
 * @author didi
 * @date 2020/9/29
 * @see ExecRecord
 */
@Resource
@Repository
public interface ExecRecordMapper {

    /**
     * 新增记录
     *
     * @param record 操作记录实体
     * @return recordId
     */
    Long insert(ExecRecord record);

    /**
     * id查询执行任务
     *
     * @param id 执行任务id
     * @return 执行记录实体
     */
    ExecRecord selectOne(Long id);

    /**
     * id查询执行任务
     *
     * @return 执行记录实体
     */
    List<ExecRecord> selectAll();

    /**
     * 根据用例id获取所属的所有执行任务
     *
     * @param caseId 用例id
     * @return 任务列表
     */
    List<ExecRecord> getRecordListByCaseId(Long caseId);


    /**
     * testcase的list接口需要展示每个case有多少任务
     *
     * @param caseIds 用例id列表
     * @return 数量统计
     */
    List<RecordNumDto> getRecordNumByCaseIds(List<Long> caseIds);

    /**
     * 脑图更新执行任务，与统计数据有关
     *
     * @param record 任务实体
     */
    void update(ExecRecord record);

    /**
     * 编辑任务的基本属性，和统计数据无关
     *
     * @param record 任务实体
     * @return 任务id
     */
    Integer edit(ExecRecord record);

    /**
     * 删除任务
     *
     * @param recordId 执行任务id
     */
    void delete(Long recordId);

    /**
     * 更新任务
     *
     * @param recordId 执行任务id
     */
    void updateById(Long recordId);

    /**
     * 批量删除执行任务
     *
     * @param recordIds 执行任务id列表
     */
    void batchDelete(List<Long> recordIds);

    /**
     * 条件筛选测试用例
     *
     * @param title 标题
     * @param owner 创建人
     * @param expectStartTime 开始时间
     * @param expectEndTime 结束时间
     * @return 筛选后的列表
     */
    List<ExecRecord> search(@Param("title") String title,
                          @Param("owner") String owner,
                          @Param("expectStartTime") Date expectStartTime,
                          @Param("expectEndTime") Date expectEndTime);
}

