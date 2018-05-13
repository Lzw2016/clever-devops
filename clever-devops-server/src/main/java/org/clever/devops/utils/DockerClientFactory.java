package org.clever.devops.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.config.GlobalConfig;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-12 19:32 <br/>
 */
public class DockerClientFactory {

    private static final DockerClientConfig DOCKER_CLIENT_CONFIG;

    static {
        GlobalConfig globalConfig = SpringContextHolder.getBean(GlobalConfig.class);
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        builder.withDockerHost(String.format("tcp://%1$s", globalConfig.getDockerUri().replace("http://", "").replace("https://", "")));
        if (StringUtils.isNotBlank(globalConfig.getDockerVersion())) {
            builder.withApiVersion(globalConfig.getDockerVersion());
        }
        builder.withDockerTlsVerify(true);
        builder.withDockerCertPath("E:\\Source\\clever-devops\\.docker");
//        builder.withDockerTlsVerify()
        DOCKER_CLIENT_CONFIG = builder.build();
    }

    /**
     * 创建一个新的 DockerClient (用完之后一定要关闭)
     */
    public static DockerClient createDockerClient() {
        return DockerClientBuilder.getInstance(DOCKER_CLIENT_CONFIG).build();
    }
}
