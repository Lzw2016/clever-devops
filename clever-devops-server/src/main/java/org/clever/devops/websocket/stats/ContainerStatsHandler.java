package org.clever.devops.websocket.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 * 管理监控容器的请求连接，该类的实例只有一个(单例)
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2018-01-07 18:30 <br/>
 */
@Component
@Slf4j
public class ContainerStatsHandler extends AbstractWebSocketHandler {

}
