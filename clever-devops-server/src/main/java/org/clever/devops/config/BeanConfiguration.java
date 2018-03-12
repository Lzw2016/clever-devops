package org.clever.devops.config;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.lang3.StringUtils;
import org.clever.devops.utils.pool.PooledDockerClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 10:37 <br/>
 */
@Configuration
public class BeanConfiguration {

    private final GlobalConfig globalConfig;
    private DockerClient dockerClient;
    // TODO 删除
    private PooledDockerClientFactory dockerClientFactory;

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

    @Bean
    public PooledDockerClientFactory getPooledDockerClientFactory() {
        if (dockerClientFactory == null) {
            DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
            builder.withDockerHost(globalConfig.getDockerHost());
            if (StringUtils.isNotBlank(globalConfig.getDockerVersion())) {
                builder.withApiVersion(globalConfig.getDockerVersion());
            }
            List<DockerClientConfig> clientConfigs = new ArrayList<>();
            clientConfigs.add(builder.build());
            dockerClientFactory = new PooledDockerClientFactory(clientConfigs);
        }
        return dockerClientFactory;
    }
}
