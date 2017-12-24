package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.dto.request.CatContainerLogReq;
import org.clever.devops.dto.response.CatContainerLogRes;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务日志查看
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-24 19:01 <br/>
 */
@Component
@Slf4j
public class ContainerLogHandler extends AbstractWebSocketHandler {

    // TODO 使用线程池 ThreadPoolTaskExecutor

    /**
     * 所有构建镜像的任务 Docker容器ID -> 查看日志任务
     */
    private static final ConcurrentHashMap<String, ContainerLogTask> CONTAINER_LOG_TASK_MAP = new ConcurrentHashMap<>();

    static {
        // 守护线程
        Thread thread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                List<String> rmList = new ArrayList<>();
                for (ConcurrentHashMap.Entry<String, ContainerLogTask> entry : CONTAINER_LOG_TASK_MAP.entrySet()) {
                    if (!entry.getValue().isAlive()) {
                        // 调用 ContainerLogTask 释放资源的方法
                        try {
                            entry.getValue().destroyTask();
                        } catch (IOException e) {
                            log.error("释放ContainerLogTask任务失败", e);
                            continue;
                        }
                        rmList.add(entry.getKey());
                    }
                }
                for (String key : rmList) {
                    CONTAINER_LOG_TASK_MAP.remove(key);
                }
                log.info("[ContainerLogHandler] 移除务数[{}] 当前正在查看日志任务数[{}]", rmList.size(), CONTAINER_LOG_TASK_MAP.size());
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
     * 建立连接后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[ContainerLogHandler] 建立连接");
    }

    /**
     * 消息处理，在客户端通过 WebSocket API 发送的消息会经过这里，然后进行相应的处理
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("[ContainerLogHandler] 消息处理 -> {}", message.getPayload());
        CatContainerLogReq catContainerLogReq = JacksonMapper.nonEmptyMapper().fromJson(message.getPayload(), CatContainerLogReq.class);
        // 校验请求消息
        if (catContainerLogReq == null) {
            sendErrorMessage(session, "请求消息格式错误");
            return;
        }
        // TODO 校验参数 CatContainerLogReq 的完整性

        // 新建查看日志任务
        ContainerLogTask containerLogTask = CONTAINER_LOG_TASK_MAP.get(catContainerLogReq.getContainerId());
        if (containerLogTask != null) {
            containerLogTask.addWebSocketSession(session);
        } else {
            containerLogTask = ContainerLogTask.newContainerLogTask(session, catContainerLogReq);
            CONTAINER_LOG_TASK_MAP.put(catContainerLogReq.getContainerId(), containerLogTask);
            containerLogTask.start();
        }
    }

    /**
     * 消息传输错误处理
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("消息传输错误处理", exception);
    }

    /**
     * 关闭连接后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
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
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        CatContainerLogRes catContainerLogRes = new CatContainerLogRes();
        catContainerLogRes.setLogText(errorMessage);
        catContainerLogRes.setComplete(true);
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(catContainerLogRes));
        try {
            session.sendMessage(textMessage);
        } catch (Throwable e) {
            throw ExceptionUtils.unchecked(e);
        }
        // 关闭连接
        WebSocketCloseSessionUtils.closeSession(session);
    }
}
