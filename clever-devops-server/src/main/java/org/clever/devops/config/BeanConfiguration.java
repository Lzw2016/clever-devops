package org.clever.devops.config;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 10:37 <br/>
 */
@Configuration
public class BeanConfiguration {

    private final GlobalConfig globalConfig;
    private DockerClient dockerClient;

    @Autowired
    public BeanConfiguration(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @Bean
    public DockerClient getDockerClient() {
        if (dockerClient != null) {
            return dockerClient;
        }
        DefaultDockerClient.Builder builder = DefaultDockerClient.builder().uri(globalConfig.getDockerUri());
        if (StringUtils.isNotBlank(globalConfig.getDockerVersion())) {
            builder.apiVersion(globalConfig.getDockerVersion());
        }
        if (globalConfig.getDockerConnectionPoolSize() != null) {
            builder.connectionPoolSize(globalConfig.getDockerConnectionPoolSize());
        }
        if (globalConfig.getDockerConnectTimeoutMillis() != null) {
            builder.connectTimeoutMillis(globalConfig.getDockerConnectTimeoutMillis());
        }
        if (globalConfig.getDockerReadTimeoutMillis() != null) {
            builder.readTimeoutMillis(globalConfig.getDockerReadTimeoutMillis());
        }
//                .dockerCertificates()
//                .registryAuthSupplier()
//                .header()
        dockerClient = builder.build();
        return dockerClient;
    }
}
