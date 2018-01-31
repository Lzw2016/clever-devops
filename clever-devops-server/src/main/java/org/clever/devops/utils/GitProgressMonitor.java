package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.websocket.ProgressMonitorToWebSocket;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.fusesource.jansi.Ansi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 监控Git处理进度 输出到WebSocket客户端
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-15 10:52 <br/>
 */
@Slf4j
public class GitProgressMonitor extends BatchingProgressMonitor {

    private static class TaskInfo implements Serializable {
        /**
         * 任务名
         */
        private String taskName;
        /**
         * 任务内容
         */
        private String progress;
        /**
         * 显示行号 从1开始
         */
        private int row;

        public TaskInfo(String taskName, String progress, int row) {
            this.taskName = taskName;
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

    public GitProgressMonitor(ProgressMonitorToWebSocket progressMonitorToWebSocket) {
        super();
        this.progressMonitorToWebSocket = progressMonitorToWebSocket;
    }

    @Override
    protected void onUpdate(String taskName, int workCurr) {
        sendLog(taskName, workCurr, false);
    }

    @Override
    protected void onEndTask(String taskName, int workCurr) {
        sendLog(taskName, workCurr, true);
    }

    @Override
    protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
        sendLog(taskName, workCurr, workTotal, percentDone, false);
    }

    @Override
    protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
        sendLog(taskName, workCurr, workTotal, percentDone, true);
    }

    /**
     * 发送进度消息
     *
     * @param taskName 任务名
     * @param progress 任务进度
     */
    private void sendMsg(String taskName, String progress) {
        Ansi ansi = Ansi.ansi();
        TaskInfo info = taskInfoList.stream().filter(task -> taskName.equals(task.taskName)).findFirst().orElse(null);
        if (info != null) {
            // 覆盖之前对应的类容
            int upLine = currentRow - info.row;
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
            info.progress = progress;
            currentRow = info.row;
            ansi.a(info.progress);
        } else {
            info = new TaskInfo(taskName, progress, taskInfoList.size() + 1);
            taskInfoList.add(info);
            int downLine = taskInfoList.size() - currentRow;
            if (downLine > 0) {
                ansi.cursorDownLine(downLine);
            }
            ansi.a(info.progress);
            currentRow = taskInfoList.size();
        }
        progressMonitorToWebSocket.sendMsg(ansi.toString());
    }

    private void sendLog(String taskName, int workCurr, boolean isEnd) {
        String progress = String.format("%1$s: %2$s", taskName, workCurr);
        if (isEnd) {
            progress = progress + ", done.";
        }
        sendMsg(taskName, progress);
    }

    private void sendLog(String taskName, int workCurr, int workTotal, int percentDone, boolean isEnd) {
        String progress = String.format("%1$s: %2$s%% (%3$s/%4$s)", taskName, percentDone, workCurr, workTotal);
        if (isEnd) {
            progress = progress + ", done.";
        }
        sendMsg(taskName, progress);
    }
}
