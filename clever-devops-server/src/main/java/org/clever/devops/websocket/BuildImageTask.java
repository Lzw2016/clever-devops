package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.dto.response.BuildImageResDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 构建镜像的任务处理类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-06 10:12 <br/>
 */
@Slf4j
public class BuildImageTask extends Thread {

    /**
     * 连接当前任务的Session集合
     */
    private Set<WebSocketSession> outSessionSet = new HashSet<>();

    /**
     * 记录所有输出日志
     */
    private StringBuilder allLogText = new StringBuilder();

    /**
     * 响应发送数据
     */
    private BuildImageResDto buildImageResDto = new BuildImageResDto();

    /**
     * 当前操作的“代码仓库”
     */
    private CodeRepository codeRepository;

    /**
     * 当前操作的“Docker镜像配置”
     */
    private ImageConfig imageConfig;

    /**
     * @param codeRepository 当前操作的“代码仓库”
     * @param imageConfig    当前操作的“Docker镜像配置”
     */
    public BuildImageTask(WebSocketSession session, CodeRepository codeRepository, ImageConfig imageConfig) {
        outSessionSet.add(session);
        this.codeRepository = codeRepository;
        this.imageConfig = imageConfig;
        // 构建响应数据
        buildImageResDto.setCodeRepository(codeRepository);
        buildImageResDto.setImageConfig(imageConfig);
    }

    /**
     * 增加一个WebSocketSession到当前任务
     */
    public void addWebSocketSession(WebSocketSession session) {
        if (outSessionSet == null) {
            outSessionSet = new HashSet<>();
        }
        outSessionSet.add(session);
    }

    /**
     * 从当前任务移除一个WebSocketSession
     *
     * @param sessionId SessionID
     */
    public boolean removeWebSocketSession(String sessionId) {
        WebSocketSession rm = outSessionSet.stream().filter(session -> Objects.equals(session.getId(), sessionId)).findFirst().orElse(null);
        return rm != null && outSessionSet.remove(rm);
    }

    /**
     * 返回连接当前任务的Session数量
     */
    public int getWebSocketSessionSize() {
        return outSessionSet == null ? 0 : outSessionSet.size();
    }

    /**
     * 返回当前任务构建的“镜像配置ID”
     */
    public Long getImageConfigId() {
        return imageConfig.getId();
    }

    /**
     * 构建镜像任务 <br/>
     * 1.下载代码 <br/>
     * 2.编译代码 <br/>
     * 3.构建镜像 <br/>
     */
    @Override
    public void run() {
        buildImageResDto.setStartTime(System.currentTimeMillis());
        //
        for (int i = 0; i < 100; i++) {
            sendLogText("构建镜像任务 i=" + i);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送日志消息
     *
     * @param logText 日志消息
     */
    private void sendLogText(String logText) {
        allLogText.append(logText);
        buildImageResDto.setLogText(logText);
        sendMessage(buildImageResDto);
    }

    /**
     * 1.发送任务结束消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param completeMessage 任务结束消息
     */
    private void sendCompleteMessage(String completeMessage) {
        allLogText.append(completeMessage);
        buildImageResDto.setCompleteMsg(completeMessage);
        // TODO 保存数据库
        // 发送消息
        sendMessage(buildImageResDto);
        // TODO 关闭所有连接
    }

    /**
     * 发送消息到所有的客户端
     *
     * @param buildImageResDto 消息对象
     */
    private void sendMessage(BuildImageResDto buildImageResDto) {
        Set<WebSocketSession> rmSet = new HashSet<>();
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(buildImageResDto));
        for (WebSocketSession session : outSessionSet) {
            if (!session.isOpen()) {
                rmSet.add(session);
                continue;
            }
            try {
                session.sendMessage(textMessage);
            } catch (Throwable e) {
                log.error("[BuildImageTask] 发送任务结束消息异常", e);
            }
        }
        // 移除关闭了的Session
        outSessionSet.removeAll(rmSet);
    }
}
