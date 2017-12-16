package org.clever.devops.websocket;

import org.eclipse.jgit.lib.BatchingProgressMonitor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 监控Git处理进度 输出到WebSocket客户端
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-15 10:52 <br/>
 */
public class GitProgressMonitor extends BatchingProgressMonitor {

    /**
     * 任务信息 taskName -> progress
     */
    private Map<String, String> taskMap = new LinkedHashMap<>();

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
        sendLog(taskName, workCurr);
    }

    @Override
    protected void onEndTask(String taskName, int workCurr) {
        sendLog(taskName, workCurr);
    }

    @Override
    protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
        sendLog(taskName, workCurr, workTotal, percentDone);
    }

    @Override
    protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
        sendLog(taskName, workCurr, workTotal, percentDone);
    }

    /**
     * 返回退格字符
     *
     * @param count 退格字符数量
     */
    private String getBackspaceStr(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append('\b');
        }
        return sb.toString();
    }

    /**
     * 发送进度消息
     *
     * @param taskName 任务名
     * @param progress 任务进度
     */
    private void sendMsg(String taskName, String progress) {
        final String oldProgress = taskMap.get(taskName);
        String backspaceStr = null;
        if (oldProgress != null) {
            backspaceStr = getBackspaceStr(oldProgress.length());
        }
        taskMap.put(taskName, progress);
        // 输出到WebSocket客户端
        if (backspaceStr != null) {
            progress = backspaceStr + progress;
        }
        progressMonitorToWebSocket.sendMsg(progress);
    }

    private void sendLog(String taskName, int workCurr) {
        final String progress = String.format("%1$s: %2$s\n", taskName, workCurr);
        sendMsg(taskName, progress);
    }

    private void sendLog(String taskName, int workCurr, int workTotal, int percentDone) {
        final String progress = String.format("%1$s: %2$s%% (%3$s/%4$s)\n", taskName, percentDone, workCurr, workTotal);
        sendMsg(taskName, progress);
    }
}
