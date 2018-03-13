package org.clever.devops.websocket.build;

import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ProgressMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.devops.websocket.ProgressMonitorToWebSocket;
import org.fusesource.jansi.Ansi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 监控Docker构建镜像进度
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-16 12:28 <br/>
 */
@Slf4j
public class BuildImageProgressMonitor implements ProgressHandler {

    /**
     * 任务信息
     */
    private List<TaskInfo> taskInfoList = new ArrayList<>();
    /**
     * 光标的当前行 从1开始
     */
    private int currentRow = 1;
    /**
     * 输出到WebSocket客户端 接口
     */
    private ProgressMonitorToWebSocket progressMonitorToWebSocket;

    public BuildImageProgressMonitor(ProgressMonitorToWebSocket progressMonitorToWebSocket) {
        super();
        this.progressMonitorToWebSocket = progressMonitorToWebSocket;
    }

    @Override
    public void progress(ProgressMessage message) {
        Ansi ansi = Ansi.ansi();
        TaskInfo taskInfo = taskInfoList.stream().filter(task -> message.id() != null && Objects.equals(message.id(), task.taskId)).findFirst().orElse(null);
        if (taskInfo != null) {
            // 覆盖之前对应的类容
            int upLine = currentRow - taskInfo.row;
            if (upLine > 0) {
                ansi.cursorUpLine(Math.abs(upLine));
            }
            if (upLine < 0) {
                ansi.cursorDownLine(Math.abs(upLine));
            }
            if (upLine == 0) {
                ansi.cursorToColumn(1);
            }
            ansi.eraseLine();
            taskInfo = getTaskInfo(message, taskInfo);
            if (taskInfo == null) {
                return;
            }
            currentRow = taskInfo.row;
            if (StringUtils.isNotBlank(taskInfo.progress)) {
                ansi.a(taskInfo.progress);
            } else if (StringUtils.isNotBlank(taskInfo.stream)) {
                ansi.a(taskInfo.stream);
            }
        } else {
            // 新增一行
            taskInfo = getTaskInfo(message, null);
            if (taskInfo == null) {
                return;
            }
            int downLine = taskInfoList.size() - currentRow;
            if (downLine > 0) {
                ansi.cursorDownLine(downLine);
            }
            if (StringUtils.isNotBlank(taskInfo.progress)) {
                ansi.a(taskInfo.progress);
            } else if (StringUtils.isNotBlank(taskInfo.stream)) {
                ansi.a(taskInfo.stream);
            }
            currentRow = taskInfoList.size();
        }
        // 发送进度
        progressMonitorToWebSocket.sendMsg(ansi.toString());
    }

    @SuppressWarnings("deprecation")
    private TaskInfo getTaskInfo(ProgressMessage message, TaskInfo taskInfo) {
        String taskId = message.id();
        String stream = message.stream();
        // id: status progress errorDetail
        StringBuilder progress = new StringBuilder();
        if (message.id() != null) {
            progress.append(message.id()).append(":");
        }
        if (message.status() != null) {
            progress.append(" ").append(message.status());
        }
        if (message.progress() != null) {
            progress.append(" ").append(message.progress());
        }
        if (message.error() != null) {
            progress.append(" ").append(message.error());
        }
        // 设置 TaskInfo
        if (stream != null) {
            if (stream.endsWith("\r\n")) {
                stream = stream.substring(0, stream.length() - 2);
            }
            if (stream.endsWith("\n") || stream.endsWith("\r")) {
                stream = stream.substring(0, stream.length() - 1);
            }
        }
        if (progress.length() <= 0 && (stream == null || stream.length() <= 0)) {
            return null;
        }
        if (taskInfo == null) {
            taskInfo = new TaskInfo();
            taskInfoList.add(taskInfo);
            taskInfo.row = taskInfoList.size();
        }
        taskInfo.taskId = taskId;
        taskInfo.stream = Ansi.ansi().a(stream).newline().toString();
        if (progress.length() > 0) {
            taskInfo.progress = progress.toString();
        }
        return taskInfo;
    }

    private static class TaskInfo implements Serializable {
        /**
         * 任务ID
         */
        private String taskId;
        /**
         * 步骤信息
         */
        private String stream;
        /**
         * 进度信息
         */
        private String progress;
        /**
         * 显示行号 从1开始
         */
        private int row;
    }
}
