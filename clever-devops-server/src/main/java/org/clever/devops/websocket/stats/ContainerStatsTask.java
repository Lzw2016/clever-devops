package org.clever.devops.websocket.stats;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Statistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.dto.request.ContainerStatsReq;
import org.clever.devops.dto.response.ContainerStatsRes;
import org.clever.devops.websocket.Task;
import org.clever.devops.websocket.TaskType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-07 22:15 <br/>
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ContainerStatsTask extends Task {

    private ContainerStatsReq containerStatsReq;
    private ContainerStatsRes containerStatsRes = new ContainerStatsRes();
    private ResultCallback resultCallback;

    /**
     * 返回当前任务ID
     */
    public static String getTaskId(ContainerStatsReq containerStatsReq) {
        if (containerStatsReq == null || StringUtils.isBlank(containerStatsReq.getContainerId())) {
            throw new BusinessException("生成TaskId失败");
        }
        return "ContainerStatsTask-" + containerStatsReq.getContainerId();
    }

    /**
     * 创建一个新的 ContainerStatsTask
     */
    public static ContainerStatsTask newContainerStatsTask(WebSocketSession session, ContainerStatsReq containerStatsReq) {
        ContainerStatsTask containerStatsTask = SpringContextHolder.getBean(ContainerStatsTask.class);
        containerStatsTask.init(session, containerStatsReq);
        return containerStatsTask;
    }

    /**
     * 初始化 ContainerStatsTask
     */
    public void init(WebSocketSession session, ContainerStatsReq containerStatsReq) {
        sessionSet.add(session);
        this.containerStatsReq = containerStatsReq;
    }

    /**
     * 返回当前任务ID
     */
    @Override
    public String getTaskId() {
        return getTaskId(containerStatsReq);
    }

    /**
     * 输出日志到 WebSocket客户端连接
     */
    @Override
    public void run() {
        resultCallback = dockerClientUtils.execute(client -> {
            StatsCmd statsCmd = client.statsCmd(containerStatsReq.getContainerId());
            return statsCmd.exec(new ResultCallback<Statistics>() {
                private Closeable closeable;

                @Override
                public void onStart(Closeable closeable) {
                    this.closeable = closeable;
                }

                @Override
                public void onNext(Statistics object) {
                    sendStatistics(object);
                }

                @Override
                public void onError(Throwable throwable) {
                    log.warn("监控出现异常", throwable);
                    sendCompleteMessage("\n监控出现异常\n" + ExceptionUtils.getStackTraceAsString(throwable));
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

    /**
     * 返回当前任务类型
     */
    @Override
    public TaskType getTaskType() {
        return TaskType.ContainerStatsTask;
    }

    /**
     * 发送监控数据到所有的客户端
     *
     * @param statistics 监控数据
     */
    private void sendStatistics(Statistics statistics) {
        containerStatsRes.setStats(statistics);
        containerStatsRes.setComplete(false);
        sendMessage(containerStatsRes);
    }

    /**
     * 发送任务结束消息到所有的客户端
     * 1.发送任务结束消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param completeMessage 任务结束消息
     */
    private void sendCompleteMessage(String completeMessage) {
        containerStatsRes.setStats(null);
        containerStatsRes.setErrorMsg(completeMessage);
        containerStatsRes.setComplete(true);
        // 发送消息
        sendMessage(containerStatsRes);
        // 关闭所有连接
        closeAllSession();
    }
}
