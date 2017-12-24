package org.clever.devops.websocket;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.dto.request.CatContainerLogReq;
import org.clever.devops.dto.response.CatContainerLogRes;
import org.clever.devops.utils.DockerClientUtils;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 服务日志查看的任务处理类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-24 19:38 <br/>
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ContainerLogTask extends Thread {

    @Autowired
    private DockerClientUtils dockerClientUtils;

    /**
     * 连接当前任务的Session集合
     */
    private ConcurrentSet<WebSocketSession> sessionSet = new ConcurrentSet<>();

    private CatContainerLogReq catContainerLogReq;
    private CatContainerLogRes catContainerLogRes = new CatContainerLogRes();
    private ResultCallback resultCallback;

    /**
     * 新建一个 ContainerLogTask
     *
     * @param session            WebSocket连接
     * @param catContainerLogReq 查看日志请求对象
     */
    public static ContainerLogTask newContainerLogTask(WebSocketSession session, CatContainerLogReq catContainerLogReq) {
        ContainerLogTask containerLogTask = SpringContextHolder.getBean(ContainerLogTask.class);
        containerLogTask.init(session, catContainerLogReq);
        return containerLogTask;
    }

    /**
     * 初始化 ContainerLogTask
     *
     * @param session            WebSocket连接
     * @param catContainerLogReq 查看日志请求对象
     */
    private void init(WebSocketSession session, CatContainerLogReq catContainerLogReq) {
        sessionSet.add(session);
        this.catContainerLogReq = catContainerLogReq;
    }

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
     * 返回连接当前任务的Session数量
     */
    public int getWebSocketSessionSize() {
        return sessionSet == null ? 0 : sessionSet.size();
    }

    /**
     * 返回当前任务查看日志的容器ID
     */
    public String getContainerId() {
        return catContainerLogReq.getContainerId();
    }

    /**
     * 输出日志到 WebSocket客户端连接
     */
    @Override
    public void run() {
        resultCallback = dockerClientUtils.execute(client -> {
            LogContainerCmd cmd = client.logContainerCmd(catContainerLogReq.getContainerId());
            cmd.withTimestamps(false);
            cmd.withFollowStream(true);
            cmd.withStdErr(true);
            cmd.withStdOut(true);
            // cmd.withSince(0);
            // cmd.withTail(0)
            // cmd.withTailAll();
            return cmd.exec(new ResultCallback<Frame>() {
                private Closeable closeable;

                @Override
                public void onStart(Closeable closeable) {
                    this.closeable = closeable;
                }

                @Override
                public void onNext(Frame object) {
                    sendLogText(new String(object.getPayload()));
                }

                @Override
                public void onError(Throwable throwable) {
                    log.warn("查看日志出现异常", throwable);
                    sendCompleteMessage("\n查看日志出现异常\n" + ExceptionUtils.getStackTraceAsString(throwable));
                }

                @Override
                public void onComplete() {
                    sendCompleteMessage("\nDocker容器已停止\n");
                }

                @Override
                public void close() throws IOException {
                    if (closeable != null) {
                        closeable.close();
                    }
                }
            });
        });
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.info("中断停止查看日志", e);
                try {
                    destroyTask();
                } catch (IOException e1) {
                    log.info("释放ContainerLogTask任务失败", e);
                }
                return;
            }
        }
    }

    /**
     * 释放任务
     */
    public void destroyTask() throws IOException {
        if (resultCallback != null) {
            resultCallback.close();
        }
    }

    /**
     * 发送日志消息到所有的客户端
     *
     * @param logText 日志消息
     */
    private void sendLogText(String logText) {
        catContainerLogRes.setLogText(logText);
        catContainerLogRes.setComplete(false);
        sendMessage(catContainerLogRes);
    }

    /**
     * 发送任务结束消息到所有的客户端
     * 1.发送任务结束消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param completeMessage 任务结束消息
     */
    private void sendCompleteMessage(String completeMessage) {
        catContainerLogRes.setLogText(completeMessage);
        catContainerLogRes.setComplete(true);
        // 发送消息
        sendMessage(catContainerLogRes);
        // 关闭所有连接
        for (WebSocketSession session : sessionSet) {
            WebSocketCloseSessionUtils.closeSession(session);
        }
    }

    /**
     * 发送消息到所有的客户端
     *
     * @param catContainerLogRes 消息对象
     */
    private void sendMessage(CatContainerLogRes catContainerLogRes) {
        if (sessionSet.size() <= 0) {
            // 已经没有连接查看日志了 中断任务
            this.interrupt();
        }
        Set<WebSocketSession> rmSet = new HashSet<>();
        for (WebSocketSession session : sessionSet) {
            if (!session.isOpen()) {
                rmSet.add(session);
                continue;
            }
            sendMessage(session, catContainerLogRes);
        }
        // 移除关闭了的Session
        sessionSet.removeAll(rmSet);
    }

    /**
     * 发送消息到指定的客户端
     *
     * @param session            WebSocket连接
     * @param catContainerLogRes 消息对象
     */
    private void sendMessage(WebSocketSession session, CatContainerLogRes catContainerLogRes) {
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(catContainerLogRes));
        try {
            session.sendMessage(textMessage);
        } catch (Throwable e) {
            log.error("[ContainerLogTask] 发送任务结束消息异常", e);
        }
    }
}
