package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 使用Git下载代码
 */
@Component
@ServerEndpoint("/build_image")
@Slf4j
public class BuildImageWebSocket {
    /**
     * 所有连接对象
     */
    private static CopyOnWriteArraySet<BuildImageWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 当前连接Session
     */
    private Session session;

    /**
     * 连接成功事件
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        System.out.println("有新链接加入!当前在线人数为" + webSocketSet.size());
    }

    /**
     * 连接关闭事件
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        System.out.println("有一链接关闭!当前在线人数为" + webSocketSet.size());
    }

    /**
     * 发生异常时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误 -> {}", error);
    }

    /**
     * 收到消息事件
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("来自客户端的消息:  | [" + webSocketSet.size() + "] | " + message);
        // 群发消息
        for (BuildImageWebSocket item : webSocketSet) {
            item.sendMessage("[" + webSocketSet.size() + "] | " + message);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
}
