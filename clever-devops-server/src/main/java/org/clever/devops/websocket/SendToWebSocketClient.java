package org.clever.devops.websocket;

/**
 * 发送消息到WebSocket客户端
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-15 11:08 <br/>
 */
public interface SendToWebSocketClient {

    /**
     * 发送消息到WebSocket客户端
     *
     * @param msg 消息内容
     */
    void sendMsg(String msg);
}
