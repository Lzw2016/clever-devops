package org.clever.devops.websocket.stats;

import lombok.extern.slf4j.Slf4j;
import org.clever.devops.websocket.Handler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 管理监控容器的请求连接，该类的实例只有一个(单例)
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2018-01-07 18:30 <br/>
 */
@Component
@Slf4j
public class ContainerStatsHandler extends Handler {

    /**
     * 消息处理，在客户端通过 WebSocket API 发送的消息会经过这里，然后进行相应的处理
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

    }

    /**
     * 1.发送一个错误消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param errorMessage 错误消息
     */
    @Override
    protected void sendErrorMessage(WebSocketSession session, String errorMessage) {

    }
}
