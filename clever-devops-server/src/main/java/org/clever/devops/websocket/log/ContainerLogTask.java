package org.clever.devops.websocket.log;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.exception.BusinessException;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.dto.request.TailContainerLogReq;
import org.clever.devops.dto.response.CatContainerLogRes;
import org.clever.devops.utils.DockerClientFactory;
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

    private static final int logsBufferMaxSize = 1000;
    private ArrayBlockingQueue<String> logsBuffer = new ArrayBlockingQueue<>(logsBufferMaxSize);

    private TailContainerLogReq tailContainerLogReq;
    private CatContainerLogRes catContainerLogRes = new CatContainerLogRes();
    private LogContainerCmd logContainerCmd;

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
        try (DockerClient dockerClientTmp = DockerClientFactory.createDockerClient()) {
            logContainerCmd = dockerClientTmp.logContainerCmd(tailContainerLogReq.getContainerId());
            // 跟随输出
            logContainerCmd.withFollowStream(true);
            // 是否显示容器时间
            logContainerCmd.withTimestamps(tailContainerLogReq.getTimestamps());
            // 显示容器错误流
            logContainerCmd.withStdErr(tailContainerLogReq.getStderr());
            // 显示容器输出流
            logContainerCmd.withStdOut(tailContainerLogReq.getStdout());
            // tail输出的行数
            logContainerCmd.withTail(logsBufferMaxSize);
            logContainerCmd.exec(new ResultCallback<Frame>() {
                private Closeable closeable;

                @Override
                public void onStart(Closeable closeable) {
                    this.closeable = closeable;
                }

                @Override
                public void onNext(Frame object) {
                    // 设置日志 输出类型
                    catContainerLogRes.setStdType(object.getStreamType().name());
                    // 得到日志字符串 Charset.forName("UTF-8")
                    String logStr = new String(object.getPayload());
                    // 处理换行符
                    int lineCount = 0;
                    String[] logsArray = logStr.split("\r\n|\n");

                    // 处理缓存 发送日志
                    int pollCount = logsBuffer.size() + logsArray.length - logsBufferMaxSize;
                    while (lineCount < pollCount) {
                        // 移除
                        logsBuffer.poll();
                        // 添加
                        logsBuffer.add(logsArray[lineCount]);
                        sendLogText(Ansi.ansi().a(logsArray[lineCount]).newline().toString());
                        lineCount++;
                    }
                    while (lineCount < logsArray.length) {
                        // 添加
                        logsBuffer.add(logsArray[lineCount]);
                        sendLogText(Ansi.ansi().a(logsArray[lineCount]).newline().toString());
                        lineCount++;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    log.warn("查看日志出现异常", throwable);
                    sendCompleteMessage(Ansi.ansi().fgRed().newline().a("查看日志出现异常").newline().a(ExceptionUtils.getStackTraceAsString(throwable)).reset().toString());
                }

                @Override
                public void onComplete() {
                    sendCompleteMessage(Ansi.ansi().fgRed().newline().a("停止查看Docker容器日志").newline().reset().toString());
                }

                @Override
                public void close() throws IOException {
                    if (closeable != null) {
                        closeable.close();
                    }
                }
            });
            // 等待所有的连接关闭
            awaitAllSessionClose();
            // 关闭连接
            dockerClientTmp.close();
            logContainerCmd.close();
        } catch (Throwable e) {
            log.error("查看日志失败", e);
            throw ExceptionUtils.unchecked(e);
        } finally {
            destroyTask();
        }
    }

    /**
     * 释放任务
     */
    @Override
    public void destroyTask() {
        if (logContainerCmd != null) {
            logContainerCmd.close();
        }
        logsBuffer.clear();
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
