package me.xiaokui.modules.system.service;

import me.xiaokui.modules.persistent.CaseBackup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 备份接口
 *
 * @author didi
 * @date 2020/11/5
 */
@Resource
@Service
public interface CaseBackupService {

    /**
     * 插入备份记录
     *
     * @param caseBackup 备份实体
     * @return 实体
     */
    CaseBackup insertBackup(CaseBackup caseBackup);

    /**
     * 获取一段时间内，某个用例备份记录
     *
     * @param
     * @param caseId 用例id
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 实体
     */
    List<CaseBackup> getBackupByCaseId(Long caseId, String startTime, String endTime);

    /**
     * 删除备份记录
     *
     * @param caseId 用例id
     * @return int
     */
    int deleteBackup(Long caseId);
}
