package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-05 21:10 <br/>
 */
@Component
@Slf4j
public class BuildImageHandler extends AbstractWebSocketHandler {

    /**
     * 建立连接后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("建立连接后");
    }

    /**
     * 消息处理，在客户端通过 WebSocket API 发送的消息会经过这里，然后进行相应的处理
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("消息处理 -> {}", message.getPayload());
    }

    /**
     * 消息传输错误处理
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("消息传输错误处理");
    }

    /**
     * 关闭连接后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("关闭连接后");
    }

    /**
     * 支持部分消息
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
