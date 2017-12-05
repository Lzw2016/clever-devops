package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-05 21:37 <br/>
 */
@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final BuildImageHandler buildImageHandler;

    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Autowired
    public WebSocketConfig(BuildImageHandler buildImageHandler, WebSocketHandshakeInterceptor webSocketHandshakeInterceptor) {
        this.buildImageHandler = buildImageHandler;
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //WebSocket通道 withSockJS()表示开启 SockJs
        registry.addHandler(buildImageHandler, "/build_image")
                .addInterceptors(webSocketHandshakeInterceptor)
//                .setAllowedOrigins()
                .withSockJS();
    }
}
