package org.clever.devops.websocket;

import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.utils.DockerClientUtils;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-07 18:42 <br/>
 */
@Slf4j
public abstract class Task extends Thread {

    @Autowired
    protected DockerClientUtils dockerClientUtils;

    /**
     * 连接当前任务的Session集合
     */
    protected ConcurrentSet<WebSocketSession> sessionSet = new ConcurrentSet<>();

    /**
     * 增加一个WebSocketSession到当前任务
     */
    public void addWebSocketSession(WebSocketSession session) {
        sessionSet.add(session);
    }

    /**
     * 从当前任务移除一个WebSocketSession
     *
     * @param sessionId SessionID
     */
    public boolean removeWebSocketSession(String sessionId) {
        WebSocketSession rm = sessionSet.stream().filter(session -> Objects.equals(session.getId(), sessionId)).findFirst().orElse(null);
        return rm != null && sessionSet.remove(rm);
    }

    /**
     * 判断Session是否已经存在
     */
    public boolean contains(WebSocketSession session) {
        return sessionSet.contains(session);
    }

    /**
     * 返回连接当前任务的Session数量
     */
    public int getWebSocketSessionSize() {
        return sessionSet == null ? 0 : sessionSet.size();
    }

    /**
     * 返回当前任务ID
     */
    public abstract String getTaskId();

    /**
     * 释放任务
     */
    public abstract void destroyTask() throws IOException;

    /**
     * 发送消息到所有的客户端
     *
     * @param object 消息对象
     */
    protected void sendMessage(Object object) {
        Set<WebSocketSession> rmSet = new HashSet<>();
        for (WebSocketSession session : sessionSet) {
            if (!session.isOpen()) {
                rmSet.add(session);
                continue;
            }
            sendMessage(session, object);
        }
        // 移除关闭了的Session
        sessionSet.removeAll(rmSet);
    }

    /**
     * 发送消息到指定的客户端
     *
     * @param session WebSocket连接
     * @param object  消息对象
     */
    protected void sendMessage(WebSocketSession session, Object object) {
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(object));
        try {
            session.sendMessage(textMessage);
        } catch (Throwable e) {
            log.error("[ContainerLogTask] 发送任务结束消息异常", e);
        }
    }

    /**
     * 返回当前任务类型
     */
    public abstract TaskType getTaskType();

    /**
     * 关闭所有的 WebSocketSession
     */
    public void closeAllSession() {
        for (WebSocketSession session : sessionSet) {
            WebSocketCloseSessionUtils.closeSession(session);
        }
        sessionSet.clear();
    }

    /**
     * 移除所有已经关闭了 WebSocketSession
     */
    public void removeCloseSession() {
        // 移除关闭了的Session
        Set<WebSocketSession> rmSet = new HashSet<>();
        for (WebSocketSession session : sessionSet) {
            if (!session.isOpen()) {
                rmSet.add(session);
            }
        }
        sessionSet.removeAll(rmSet);
    }

    /**
     * 等待所有的连接关闭(会阻塞当前线程)
     */
    public void awaitAllSessionClose() {
        while (getWebSocketSessionSize() > 0) {
            try {
                Thread.sleep(1000);
                // 移除关闭了的Session
                removeCloseSession();
            } catch (InterruptedException e) {
                log.info("休眠失败", e);
            }
        }
    }
}
