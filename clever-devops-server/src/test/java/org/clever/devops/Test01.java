package org.clever.devops;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.mapper.JacksonMapper;
import org.junit.Test;

import java.io.IOException;

/**
 * 作者：lizw <br/>
 * 创建时间：2017/11/27 11:40 <br/>
 */
@Slf4j
public class Test01 {

    private static final String dockerHost = "tcp://10.255.8.215:2375";
    private static final String version = "1.33";

    @Test
    public void test01() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
//                .withApiVersion(version)
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        Info info = dockerClient.infoCmd().exec();
        log.info(JacksonMapper.nonEmptyMapper().toJson(info));
        dockerClient.close();
    }

    @Test
    public void test02() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
//                .withApiVersion(version)
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
//        Info info = dockerClient.
//        log.info(JacksonMapper.nonEmptyMapper().toJson(info));
        dockerClient.close();
    }
}
