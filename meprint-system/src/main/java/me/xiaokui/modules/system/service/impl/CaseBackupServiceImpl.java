package me.xiaokui.modules.system.service.impl;

import me.xiaokui.modules.mapper.CaseBackupMapper;
import me.xiaokui.modules.system.service.CaseBackupService;
import me.xiaokui.modules.util.TimeUtil;
import me.xiaokui.modules.persistent.CaseBackup;
import me.xiaokui.modules.system.service.CaseBackupService;
import me.xiaokui.modules.mapper.CaseBackupMapper;
import me.xiaokui.modules.util.TimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 备份记录
 *
 * @author didi
 * @date 2020/11/5
 */
@Service
public class CaseBackupServiceImpl implements CaseBackupService {


    private CaseBackupMapper caseBackupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CaseBackup insertBackup(CaseBackup caseBackup) {
        int backupId = caseBackupMapper.insert(caseBackup);
        caseBackup.setCaseId((long) backupId);
        return caseBackup;
    }

    @Override
    public List<CaseBackup> getBackupByCaseId(Long caseId, String beginTime, String endTime) {
        return caseBackupMapper.selectByCaseId(caseId, transferTime(beginTime), transferTime(endTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBackup(Long caseId ) {
        return caseBackupMapper.updateByCaseId(caseId);
    }

    private Date transferTime(String time) {
        if (time == null) {
            return null;
        }
        return TimeUtil.transferStrToDateInSecond(time);
    }
}
