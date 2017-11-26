package org.clever.devops;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.spring.SpringContextHolder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

/**
 * 应用启动类
 * Created by lzw on 2017/2/25.
 */
@Slf4j
@EnableTransactionManagement
@MapperScan(basePackages = "org.clever.devops.mapper")
@SpringBootApplication(scanBasePackages = {"org.clever"})
public class DevopsStartApp {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        ApplicationContext ctx = SpringApplication.run(DevopsStartApp.class, args);
        SpringContextHolder.setApplicationContext(ctx);
        log.info("### 服务启动完成 === " + SpringContextHolder.getApplicationContext());
    }
}
