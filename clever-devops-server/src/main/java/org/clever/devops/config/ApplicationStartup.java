package org.clever.devops.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * 服务启动事件
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2017/6/22 14:47 <br/>
 */
@Slf4j
@Service
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("### 服务启动成功");
    }
}
