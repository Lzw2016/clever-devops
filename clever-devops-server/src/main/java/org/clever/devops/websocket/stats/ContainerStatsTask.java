package org.clever.devops.websocket.stats;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.websocket.Task;
import org.clever.devops.websocket.TaskType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-07 22:15 <br/>
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ContainerStatsTask extends Task {

//    /**
//     * 返回当前任务ID
//     */
//    public static String getTaskId() {
//        return null;
//    }

    /**
     * 返回当前任务ID
     */
    @Override
    public String getTaskId() {
        return null;
    }

    /**
     * 输出日志到 WebSocket客户端连接
     */
    @Override
    public void run() {

    }

    /**
     * 释放任务
     */
    @Override
    public void destroyTask() {

    }

    /**
     * 返回当前任务类型
     */
    @Override
    public TaskType getTaskType() {
        return TaskType.ContainerStatsTask;
    }
}
