package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-07 19:04 <br/>
 */
@Slf4j
public abstract class Handler extends AbstractWebSocketHandler {

    /**
     * 所有的任务
     */
    private static final ConcurrentHashMap<String, Task> TASK_MAP = new ConcurrentHashMap<>();

    static {
        // 守护线程 TODO 使用线程池 ThreadPoolTaskExecutor
        Thread thread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                List<String> rmList = new ArrayList<>();
                int allSessionCount = 0;
                for (ConcurrentHashMap.Entry<String, Task> entry : TASK_MAP.entrySet()) {
                    String key = entry.getKey();
                    Task task = entry.getValue();
                    allSessionCount += task.getWebSocketSessionSize();
                    if (!task.isAlive()) {
                        try {
                            task.destroyTask();
                            rmList.add(key);
                        } catch (IOException e) {
                            log.error(String.format("释放%1$s任务失败", task.getClass().getSimpleName()), e);
                        }
                    }
                }
                for (String key : rmList) {
                    TASK_MAP.remove(key);
                }
                log.info(String.format("连接总数[%1$s] 任务总数[%2$s] 移除任务数[%3$s]", allSessionCount, TASK_MAP.size(), rmList.size()));
                try {
                    Thread.sleep(1000 * 3);
                } catch (Throwable e) {
                    log.error("休眠失败", e);
                }
            }
        });
        thread.start();
    }

    /**
     * 获取所有的任务(Task)
     */
    protected static Collection<Task> getAllTask() {
        return TASK_MAP.values();
    }

    /**
     * 根据taskId获取 Task对象
     */
    protected static Task getTaskByTaskId(String taskId) {
        return TASK_MAP.get(taskId);
    }

    /**
     * 添加任务 <br/>
     * 启动任务 <br/>
     */
    protected static void putAndStartTask(Task task) {
        TASK_MAP.put(task.getTaskId(), task);
        task.start();
    }

    /**
     * 获取任务的总数量
     */
    protected static int getTaskCount() {
        return TASK_MAP.size();
    }

    /**
     * 获取任务的总数量
     */
    protected static int getTaskCount(TaskType taskType) {
        Long count = TASK_MAP.values().stream().filter(task -> task.getTaskType().equals(taskType)).count();
        return count.intValue();
    }

    /**
     * 建立连接后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("建立连接");
    }

    /**
     * 消息传输错误处理
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("消息传输错误处理", exception);
    }

    /**
     * 关闭连接后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("关闭连接后");
        WebSocketCloseSessionUtils.closeSession(session);
    }

    /**
     * 支持部分消息
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 1.发送一个错误消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param errorMessage 错误消息
     */
    protected abstract void sendErrorMessage(WebSocketSession session, String errorMessage);
}
