package org.clever.devops.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 12:44 <br/>
 */
@Component
@ConfigurationProperties(prefix = "devops")
@Data
public class GlobalConfig {

    /**
     * 最大构建镜像的任务
     */
    private int maxBuildImageTask;

    /**
     * 下载代码的路径
     */
    private String codeDownloadPath;

    /**
     * Maven的Settings文件路径
     */
    private String mavenSettingsPath;

    /**
     * Docker 连接地址
     */
    private String dockerUri;

    /**
     * Docker API 版本
     */
    private String dockerVersion;

    private Integer dockerConnectionPoolSize;

    private Integer dockerConnectTimeoutMillis;

    private Integer dockerReadTimeoutMillis;

    /**
     * Docker TLS认证文件路径
     */
    private String dockerCertPath;

    /**
     * Docker TLS认证 ca.pem
     */
    private String dockerCaCertName = "ca.pem";

    /**
     * Docker TLS认证 cert.pem
     */
    private String dockerClientCertName = "cert.pem";

    /**
     * Docker TLS认证 key.pem
     */
    private String dockerClientKeyName = "key.pem";
}
