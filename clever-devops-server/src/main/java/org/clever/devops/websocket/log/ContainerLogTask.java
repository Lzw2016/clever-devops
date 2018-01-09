package org.clever.devops.websocket.log;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.dto.request.CatContainerLogReq;
import org.clever.devops.dto.response.CatContainerLogRes;
import org.clever.devops.websocket.Task;
import org.clever.devops.websocket.TaskType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;

/**
 * 服务日志查看的任务处理类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-24 19:38 <br/>
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ContainerLogTask extends Task {

    private CatContainerLogReq catContainerLogReq;
    private CatContainerLogRes catContainerLogRes = new CatContainerLogRes();
    private ResultCallback resultCallback;

    /**
     * 返回当前任务ID
     */
    public static String getTaskId(CatContainerLogReq catContainerLogReq) {
        if (catContainerLogReq == null || StringUtils.isBlank(catContainerLogReq.getContainerId())) {
            throw new BusinessException("生成TaskId失败");
        }
        return "ContainerLogTask-" + catContainerLogReq.getContainerId();
    }

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
     * 返回当前任务ID
     */
    @Override
    public String getTaskId() {
        return getTaskId(catContainerLogReq);
    }

    /**
     * 输出日志到 WebSocket客户端连接
     */
    @Override
    public void run() {
        resultCallback = dockerClientUtils.execute(client -> {
            LogContainerCmd cmd = client.logContainerCmd(catContainerLogReq.getContainerId());
            cmd.withFollowStream(true);
            cmd.withTimestamps(catContainerLogReq.getTimestamps());
            cmd.withStdErr(catContainerLogReq.getStderr());
            cmd.withStdOut(catContainerLogReq.getStdout());
            cmd.withSince(catContainerLogReq.getSince());
            cmd.withTail(catContainerLogReq.getTail());
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
        // 等待所有的连接关闭
        awaitAllSessionClose();
    }

    /**
     * 释放任务
     */
    @Override
    public void destroyTask() throws IOException {
        if (resultCallback != null) {
            resultCallback.close();
        }
        closeAllSession();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.ContainerLogTask;
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
        closeAllSession();
    }
}
