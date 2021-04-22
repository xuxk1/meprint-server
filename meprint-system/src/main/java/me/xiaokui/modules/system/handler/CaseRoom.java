package me.xiaokui.modules.system.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by didi on 2021/3/27.
 */
public class CaseRoom extends Room {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseRoom.class);

    public CaseRoom(Long id) {
        super(id);
    }

    @Override
    public Player createAndAddPlayer(Client client) {
        Player p = super.createAndAddPlayer(client);
        String caseContent = testCaseContent != null ? testCaseContent : testCase.getCaseContent();
        p.getClient().sendMessage(caseContent);
        LOGGER.info(Thread.currentThread().getName() + ": 新的用户加入成功，传输用例内容： " + testCaseContent);
        return p;
    }

    @Override
    protected void internalRemovePlayer(Player p) {
        super.internalRemovePlayer(p);

        // 如果是最后一个用户离开，需要关闭广播任务
        if (players.size() == 0) {
            if (testCaseContent != null) {
                LOGGER.info(Thread.currentThread().getName() + ": 当前的用例内容是：" + testCaseContent);
                testCase.setCaseContent(testCaseContent);
                testCase.setGmtModified(new Date(System.currentTimeMillis()));
                int ret = caseMapper.update(testCase);
                LOGGER.info(Thread.currentThread().getName() + ": 数据库用例内容更新。 ret = " + ret);
            }
            LOGGER.info(Thread.currentThread().getName() + ": 最后一名用户离开，关闭。");
        }

        // 广播有用户离开
        broadcastRoomMessage("当前用户数:" + players.size() + "。用例编辑者 " + p.getClient().getClientName() + " 离开");
    }
}
