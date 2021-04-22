package me.xiaokui.modules.system.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.util.BitBaseUtil;
import me.xiaokui.modules.persistent.TestCase;
import me.xiaokui.modules.system.service.RecordService;
import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.util.BitBaseUtil;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by didi on 2021/3/22.
 */
public abstract class Room {
    private static final Logger LOGGER = LoggerFactory.getLogger(Room.class);

    private final ReentrantLock roomLock = new ReentrantLock();
    private volatile boolean closed = false;

    private static final boolean BUFFER_MESSAGES = true;
    private final Timer messageBroadcastTimer = new Timer();
    private volatile boolean locked = false;
    private volatile String locker = "";
    private static final int TIMER_DELAY = 30;
    private TimerTask activeBroadcastTimerTask;

    private static final int MAX_PLAYER_COUNT = 100;
    public final List<Player> players = new ArrayList<>();
    public final Map<Session, Client> cs = new ConcurrentHashMap<>();

    public static TestCaseMapper caseMapper;
    public static RecordService recordService;

    protected String testCaseContent;
    protected TestCase testCase;

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean getLock() {
        return this.locked;
    }
    public String getLocker() {
        return this.locker;
    }
    public void setLocker(String locker) {
        this.locker = locker;
    }
    // id 前面部分是case id；后面部分是record id
    public Room(Long id) {
        long caseId = BitBaseUtil.getLow32(id);
        if (testCase != null) {
            return;
        }
        testCase = caseMapper.selectOne(caseId);
        String res = testCase.getCaseContent();
        if (StringUtils.isEmpty(res)) {
            LOGGER.error(Thread.currentThread().getName() + ": 用例内容为空");
        }
    }

    private TimerTask createBroadcastTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        broadcastTimerTick();
                    }
                });
            }
        };
    }

    public String getTestCaseContent() {
        return testCaseContent;
    }

    public Player createAndAddPlayer(Client client) {
        if (players.size() >= MAX_PLAYER_COUNT) {
            throw new IllegalStateException("Maximum player count ("
                    + MAX_PLAYER_COUNT + ") has been reached.");
        }
        LOGGER.info(Thread.currentThread().getName() + ": 有新的用户加入。session id: " + client.getSession().getId());
        Player p = new Player(this, client);

        // 通知消息
        broadcastRoomMessage( "当前用户数： " + (players.size() + 1) + "。新用户是：" + client.getClientName());

        players.add(p);
        cs.put(client.getSession(), client);

        // 如果广播任务没有被调度，则新建一个
        if (activeBroadcastTimerTask == null) {
            activeBroadcastTimerTask = createBroadcastTimerTask();
            messageBroadcastTimer.schedule(activeBroadcastTimerTask,
                    TIMER_DELAY, TIMER_DELAY);
        }

        // 发送当前用户数
        String content = String.valueOf(players.size());
        p.sendRoomMessageSync("当前用户数：" + content);

        return p;
    }

    protected void internalRemovePlayer(Player p) {

        boolean removed = players.remove(p);
        assert removed;

        cs.remove(p.getClient().getSession());
        LOGGER.info(Thread.currentThread().getName() + ": 有用户 " + p.getClient().getClientName() + " 离开 session id:" + p.getClient().getSession().getId());

        // 如果是最后一个用户离开，需要关闭广播任务
        if (players.size() == 0) {
            // 关闭任务
            // todo： 为了避免timer cancel时还有任务没有执行完，需要在查询invokeAndWait的任务信息
            closed = true;
            activeBroadcastTimerTask.cancel();
            activeBroadcastTimerTask = null;
        }

        // 广播有用户离开
//        broadcastRoomMessage("用户离开：" + p.getClient().getSession().getId());
    }

    // 直接广播发送内容，不经过buffer池。适用于所有消息都是一致的场景。
    protected void broadcastRoomMessage(String content) {
        for (Player p : players) {
            p.sendRoomMessageSync(content);
        }
    }

    private void internalHandleMessage(Player p, String msg,
                                           long msgId) {
        p.setLastReceivedMessageId(msgId);

        //todo: testCase.apply(msg) 新增如上的方法.

        broadcastMessage(msg);
    }

    private void internalHandleCtrlMessage(String msg) {
        int seperateIndex = msg.indexOf('|');
        String sendSessionId = msg.substring(0, seperateIndex);

        for (Player p : players) {
            if (sendSessionId.equals(p.getClient().getSession().getId())) {
                p.getBufferedMessages().add("2" + "success");
//                p.sendRoomMessage("2" + "success");
            } else {
                p.getBufferedMessages().add("2" + msg.substring(seperateIndex + 1));
//                p.sendRoomMessage("2" + "lock");
            }
        }
    }

    private void broadcastMessage(String msg) {
        if (!BUFFER_MESSAGES) {
            String msgStr = msg.toString();

            for (Player p : players) {
                String s = String.valueOf(p.getLastReceivedMessageId())
                        + "," + msgStr;
                p.sendRoomMessageSync(s); // 直接发送，不放到buffer
            }
        } else {
            int seperateIndex = msg.indexOf('|');
            String sendSessionId = msg.substring(0, seperateIndex);
            JSONObject request = JSON.parseObject(msg.substring(seperateIndex + 1));
            JSONArray patch = (JSONArray) request.get("patch");
            long currentVersion = ((JSONObject) request.get("case")).getLong("base");
            testCaseContent = ((JSONObject) request.get("case")).toJSONString().replace("\"base\":" + currentVersion, "\"base\":" + (currentVersion + 1));
            for (Player p : players) {
                if (sendSessionId.equals(p.getClient().getSession().getId())) { //ack消息
                    String msgAck = "[[{\"op\":\"replace\",\"path\":\"/base\",\"value\":" + (currentVersion + 1) + "}]]";
                    p.getBufferedMessages().add(msgAck);
                } else { // notify消息
                    String msgNotify = patch.toJSONString().replace("[[{", "[[{\"op\":\"replace\",\"path\":\"/base\",\"value\":" + (currentVersion + 1) + "},{");
                    p.getBufferedMessages().add(msgNotify);
                }
            }
        }
    }

    private void broadcastTimerTick() {
        // 给每一个player广播消息
        for (Player p : players) {
            StringBuilder sb = new StringBuilder();
            List<String> caseMessages = p.getBufferedMessages();
//            LOGGER.info("当前的消息池消息数量为：" + caseMessages.size());
            if (caseMessages.size() > 0) {
                for (int i = 0; i < caseMessages.size(); i++) {
                    String msg = caseMessages.get(i);

//                    String s = String.valueOf(p.getLastReceivedMessageId())
//                            + "," + msg.toString();
                    if (i > 0) {
                        sb.append("|");
                        LOGGER.error(Thread.currentThread().getName() + ": client: " + p.getClient().getClientName() + " 此处可能会有问题，待处理 by肖锋. sb: " + sb);
                    }

                    sb.append(msg);
                }

                caseMessages.clear();

                p.sendRoomMessageSync(sb.toString());
            }
        }

    }

    private List<Runnable> cachedRunnables = null;

    public void invokeAndWait(Runnable task)  {

        // 检查当前线程是否持有房间锁，如果持有，则不能直接允许runnable，要先缓存住，直到前一个任务执行完成。
        if (roomLock.isHeldByCurrentThread()) {

            if (cachedRunnables == null) {
                cachedRunnables = new ArrayList<>();
            }
            cachedRunnables.add(task);

        } else {
            roomLock.lock();
            try {
                // 确保数据一致性。已经有任务执行时，会卡在下面的task.run，然后其他新进来的任务cache住
                cachedRunnables = null;

                if (!closed) {
                    task.run();
                }

                // 执行缓存的任务
                if (cachedRunnables != null) {
                    for (Runnable cachedRunnable : cachedRunnables) {
                        if (!closed) {
                            cachedRunnable.run();
                        }
                    }
                    cachedRunnables = null;
                }
            } finally {
                roomLock.unlock();
            }
        }
    }

    public String getRoomPlayersName() {
        Set<String> playerNames = new HashSet<>();
        for (Player p: players) {
            playerNames.add(p.getClient().getClientName());
        }
        return StringUtil.join(playerNames.toArray(), ",");
    }

    public static final class Player {

        /**
         * player所属的room
         */
        private Room room;

        /**
         * room缓存的最后一个msg id
         */
        private long lastReceivedMessageId = 0;

        private final Client client;
        private final long enterTimeStamp;

//        private final boolean isRecord;

        /**
         * 缓存的将要被timer处理的消息
         */
        private final List<String> bufferedMessages = new ArrayList<>();

        private List<String> getBufferedMessages() {
            return bufferedMessages;
        }

        private Player(Room room, Client client) {
            this.room = room;
            this.client = client;
            this.enterTimeStamp = System.currentTimeMillis();
//            isRecord = client.getRecordId();
        }

        public Room getRoom() {
            return room;
        }

        public Client getClient() {
            return client;
        }

        /**
         * client断开连接时，需要移除的player
         */
        public void removeFromRoom() {
            if (room != null) {
                LOGGER.info("当前离开用户 " + this.getClient().getClientName() +  " 的使用时长是：" + String.valueOf(System.currentTimeMillis() - this.enterTimeStamp));
                room.internalRemovePlayer(this);
                room = null;
            }
        }

        private long getLastReceivedMessageId() {
            return lastReceivedMessageId;
        }
        private void setLastReceivedMessageId(long value) {
            lastReceivedMessageId = value;
        }


        /**
         * 处理客户端发送的消息，并将消息广播给所有players
         *
         * @param msg   接收的消息
         * @param msgId 消息id
         */
        public void handleMessage(String msg, long msgId) {
            room.internalHandleMessage(this, msg, msgId);
        }

        public void handleCtrlMessage(String msg) {
            room.internalHandleCtrlMessage(msg);
        }

        /**
         * 发送room的消息
         * @param content
         */
        public void sendRoomMessageSync(String content) {
            Objects.requireNonNull(content);

            client.sendMessage(content);
        }

        public void sendRoomMessageAsync(String content) {
            Objects.requireNonNull(content);

            this.getBufferedMessages().add(content);
        }
    }
}
