package org.clever.devops.websocket.stats;

import com.spotify.docker.client.messages.ContainerStats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.exception.BusinessException;
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
        while (sessionSet.size() > 0) {
            try {
                ContainerStats containerStats = dockerClient.stats(containerStatsReq.getContainerId());
                sendStatistics(containerStats);
                closeAllSession();
            } catch (Throwable e) {
                log.warn("监控出现异常", e);
                sendCompleteMessage("\n监控出现异常\n" + ExceptionUtils.getStackTraceAsString(e));
            }
        }
        destroyTask();
    }

    /**
     * 释放任务
     */
    @Override
    public void destroyTask() {
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
     * @param containerStats 监控数据
     */
    private void sendStatistics(ContainerStats containerStats) {
        containerStatsRes.setStats(containerStats);
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
