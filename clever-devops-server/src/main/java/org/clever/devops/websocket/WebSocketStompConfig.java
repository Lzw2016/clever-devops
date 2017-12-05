//package org.clever.devops.websocket;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.converter.MessageConverter;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//
//import java.util.List;
//
///**
// * 支持 WebSocket 的配置
// * <p>
// * 作者： lzw<br/>
// * 创建时间：2017-12-05 13:44 <br/>
// */
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketStompConfig extends AbstractWebSocketMessageBrokerConfigurer {
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        // 定义了服务端接收地址的前缀，也即客户端给服务端发消息的地址前缀
//        config.setApplicationDestinationPrefixes("/app");
//        // 定义了两个客户端订阅地址的前缀信息，也就是客户端接收服务端发送消息的前缀信息
//        config.enableSimpleBroker("/topic");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        // 添加一个/端点，客户端就可以通过这个端点来进行连接; withSockJS作用是添加SockJS支持
//        registry.addEndpoint("/").withSockJS();
//    }
//
//    /**
//     * 配置消息转换器
//     */
//    @Override
//    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
//        return true;
//    }
//}
