package org.clever.devops.config;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
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

    @Autowired
    public BeanConfiguration(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    private PooledDockerClientFactory dockerClientFactory;

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
