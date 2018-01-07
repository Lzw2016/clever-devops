package org.clever.devops.websocket.build;

import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.devops.utils.BackspaceStringUtils;
import org.clever.devops.websocket.ProgressMonitorToWebSocket;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控Docker构建镜像进度
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-16 12:28 <br/>
 */
@Slf4j
public class BuildImageProgressMonitor extends BuildImageResultCallback {

    /**
     * stream 步骤信息
     */
    private List<String> stream = new ArrayList<>();

    /**
     * 任务信息 ID -> progress (id: status progress errorDetail)
     */
    private Map<String, String> taskMap = new LinkedHashMap<>();

    /**
     * 上一次发送的进度消息
     */
    private String oldProgressMessage;

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
        super.onNext(item);
        String oldProgress = taskMap.get(item.getId());
        String progress = null;
        // 设置步骤信息
        if (item.getStream() != null) {
            stream.add(item.getStream());
        }
        // 处理进度字段
        String itemId = item.getId() == null ? "" : item.getId();
        String status = item.getStatus() == null ? "" : item.getStatus();
        @SuppressWarnings("deprecation")
        String progressText = item.getProgress() == null ? "" : item.getProgress();
        // 设置任务进度信息
        ResponseItem.ProgressDetail progressDetail = item.getProgressDetail();
        if (progressDetail != null || StringUtils.isNotBlank(status)) {
            // 格式 id: status progress
            progress = String.format("%1$s: %2$s %3$s", itemId, status, progressText);
        }
        // 设置错误信息 id: status progress errorDetail
        if (item.getErrorDetail() != null) {
            String errorDetail = item.getErrorDetail() == null ? "" : String.format("[code=%1$s, message=%2$s]", item.getErrorDetail().getCode(), item.getErrorDetail().getMessage());
            if (progress != null) {
                progress = String.format("%1$s %2$s", progress, errorDetail);
            } else if (oldProgress != null) {
                progress = String.format("%1$s %2$s", oldProgress, errorDetail);
            } else {
                // 格式 id: status progress errorDetail
                progress = String.format("%1$s: %2$s %3$s %4$s", itemId, status, progressText, errorDetail);
            }
        }
        // 设置进度
        if (progress != null) {
            taskMap.put(item.getId(), progress);
        }
        // 发送进度
        sendMsg();
    }

    /**
     * 发送进度消息
     */
    private void sendMsg() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> task : taskMap.entrySet()) {
            stringBuilder.append(task.getValue()).append("\n");
        }
        stringBuilder.append("\n");
        for (String str : stream) {
            stringBuilder.append(str);
        }
        // 输出 发送进度消息
        String backspaceStr = "";
        if (oldProgressMessage != null) {
            backspaceStr = BackspaceStringUtils.getBackspaceStr(oldProgressMessage.length());
        }
        oldProgressMessage = stringBuilder.toString();
        progressMonitorToWebSocket.sendMsg(backspaceStr + oldProgressMessage);
    }
}
