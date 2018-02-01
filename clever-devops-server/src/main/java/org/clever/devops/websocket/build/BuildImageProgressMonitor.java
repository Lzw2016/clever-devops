package org.clever.devops.websocket.build;

import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.devops.websocket.ProgressMonitorToWebSocket;
import org.fusesource.jansi.Ansi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 监控Docker构建镜像进度
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-16 12:28 <br/>
 */
@Slf4j
public class BuildImageProgressMonitor extends BuildImageResultCallback {

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

        public TaskInfo() {
        }

        public TaskInfo(String stream, String taskId, String progress, int row) {
            this.stream = stream;
            this.taskId = taskId;
            this.progress = progress;
            this.row = row;
        }
    }

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
    public void onNext(BuildResponseItem item) {
        System.out.println(item);
        super.onNext(item);
        Ansi ansi = Ansi.ansi();
        TaskInfo taskInfo = taskInfoList.stream().filter(task -> item.getId() != null && item.getId().equals(task.taskId)).findFirst().orElse(null);
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
            taskInfo = getTaskInfo(item, taskInfo);
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
            taskInfo = getTaskInfo(item, null);
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
    private TaskInfo getTaskInfo(BuildResponseItem item, TaskInfo taskInfo) {
        String taskId = item.getId();
        String stream = item.getStream();
        // id: status progress errorDetail
        StringBuilder progress = new StringBuilder();
        if (item.getId() != null) {
            progress.append(item.getId()).append(":");
        }
        if (item.getStatus() != null) {
            progress.append(" ").append(item.getStatus());
        }
        if (item.getProgress() != null) {
            progress.append(" ").append(item.getProgress());
        }
        if (item.getErrorDetail() != null) {
            progress.append(" ").append(item.getErrorDetail());
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
        taskInfo.stream = stream;
        if (progress.length() > 0) {
            taskInfo.progress = progress.toString();
        }
        return taskInfo;
    }
}
