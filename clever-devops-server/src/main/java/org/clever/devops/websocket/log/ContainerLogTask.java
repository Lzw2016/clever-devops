package org.clever.devops.websocket.log;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.dto.request.TailContainerLogReq;
import org.clever.devops.dto.response.CatContainerLogRes;
import org.clever.devops.websocket.Task;
import org.clever.devops.websocket.TaskType;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

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

    private static final int logsBufferMaxSize = 10000;
    private ArrayBlockingQueue<String> logsBuffer = new ArrayBlockingQueue<>(logsBufferMaxSize);

    private TailContainerLogReq tailContainerLogReq;
    private CatContainerLogRes catContainerLogRes = new CatContainerLogRes();
    private ResultCallback resultCallback;

    /**
     * 返回当前任务ID
     */
    public static String getTaskId(TailContainerLogReq tailContainerLogReq) {
        if (tailContainerLogReq == null || StringUtils.isBlank(tailContainerLogReq.getContainerId())) {
            throw new BusinessException("生成TaskId失败");
        }
        return "ContainerLogTask-" + tailContainerLogReq.getContainerId();
    }

    /**
     * 新建一个 ContainerLogTask
     *
     * @param session             WebSocket连接
     * @param tailContainerLogReq 查看日志请求对象
     */
    public static ContainerLogTask newContainerLogTask(WebSocketSession session, TailContainerLogReq tailContainerLogReq) {
        ContainerLogTask containerLogTask = SpringContextHolder.getBean(ContainerLogTask.class);
        containerLogTask.init(session, tailContainerLogReq);
        return containerLogTask;
    }

    /**
     * 初始化 ContainerLogTask
     *
     * @param session             WebSocket连接
     * @param tailContainerLogReq 查看日志请求对象
     */
    private void init(WebSocketSession session, TailContainerLogReq tailContainerLogReq) {
        sessionSet.add(session);
        this.tailContainerLogReq = tailContainerLogReq;
    }

    /**
     * 增加一个WebSocketSession到当前任务
     */
    @Override
    public void addWebSocketSession(WebSocketSession session) {
        sessionSet.add(session);
        final Ansi ansi = Ansi.ansi();
        logsBuffer.forEach(logs -> ansi.a(logs).newline());
        sendMessage(session, new CatContainerLogRes(ansi.toString(), false));

    }

    /**
     * 返回当前任务ID
     */
    @Override
    public String getTaskId() {
        return getTaskId(tailContainerLogReq);
    }

    /**
     * 输出日志到 WebSocket客户端连接
     */
    @Override
    public void run() {
        resultCallback = dockerClientUtils.execute(client -> {
            LogContainerCmd cmd = client.logContainerCmd(tailContainerLogReq.getContainerId());
            cmd.withFollowStream(true);
            cmd.withTimestamps(tailContainerLogReq.getTimestamps());
            cmd.withStdErr(tailContainerLogReq.getStderr());
            cmd.withStdOut(tailContainerLogReq.getStdout());
//            cmd.withSince(tailContainerLogReq.getSince());
//            cmd.withTail(tailContainerLogReq.getTail());
            // cmd.withTailAll();
            return cmd.exec(new ResultCallback<Frame>() {
                private Closeable closeable;

                @Override
                public void onStart(Closeable closeable) {
                    this.closeable = closeable;
                }

                @Override
                public void onNext(Frame object) {
                    int i = 0;
                    // 处理换行符
                    String[] logsArray = new String(object.getPayload()).split("\r\n|\n");
                    int pollCount = logsBuffer.size() + logsArray.length - logsBufferMaxSize;
                    while (i < pollCount) {
                        // 移除
                        logsBuffer.poll();
                        // 添加
                        logsBuffer.add(logsArray[i]);
                        sendLogText(Ansi.ansi().a(logsArray[i]).newline().toString());
                        i++;
                    }
                    while (i < logsArray.length) {
                        // 添加
                        logsBuffer.add(logsArray[i]);
                        sendLogText(Ansi.ansi().a(logsArray[i]).newline().toString());
                        i++;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    log.warn("查看日志出现异常", throwable);
                    sendCompleteMessage(Ansi.ansi().fgRed().newline().a("查看日志出现异常").newline().a(ExceptionUtils.getStackTraceAsString(throwable)).reset().toString());
                }

                @Override
                public void onComplete() {
                    sendCompleteMessage(Ansi.ansi().fgRed().newline().a("Docker容器已停止").newline().reset().toString());
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
