package org.clever.devops.config;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 10:37 <br/>
 */
@Configuration
@Slf4j
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
        if (StringUtils.isNotBlank(globalConfig.getDockerCertPath())) {
            try {
                URI path = this.getClass().getResource(globalConfig.getDockerCertPath()).toURI();
                log.info("### 加载Docker TLS认证文件 [{}]", path.toString());
                DockerCertificatesStore dockerCertificates = DockerCertificates
                        .builder()
                        .caCertPath(Paths.get(path).resolve(globalConfig.getDockerCaCertName()))
                        .clientKeyPath(Paths.get(path).resolve(globalConfig.getDockerClientCertName()))
                        .clientCertPath(Paths.get(path).resolve(globalConfig.getDockerClientKeyName()))
                        .build().orNull();
                builder.dockerCertificates(dockerCertificates);
            } catch (DockerCertificateException e) {
                log.error("加载Docker TLS认证文件异常", e);
            } catch (URISyntaxException e) {
                log.error("读取Docker TLS认证文件失败", e);
            }
        }
        dockerClient = builder.build();
        return dockerClient;
    }
}
