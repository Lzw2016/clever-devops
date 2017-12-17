package org.clever.devops;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Swarm;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.utils.DockerClientUtils;
import org.clever.devops.websocket.BuildImageProgressMonitor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 作者：lizw <br/>
 * 创建时间：2017/11/27 11:40 <br/>
 */
@Slf4j
public class Test01 {

    private static final String dockerHost = "tcp://192.168.159.131:2375";
    private static final String version = "1.33";

    public DockerClient newDockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withApiVersion(version)
                .build();

        return DockerClientBuilder.getInstance(config).build();
    }

    @Test
    public void test01() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        String dockerfilePath = "E:\\Source\\clever-devops\\Dockerfile";
        Map<String, String> labels = new HashMap<>();
        labels.put("labels1", "value1");
        labels.put("labels2", "value2");
        labels.put("labels3", "value3");
        Set<String> tags = new HashSet<>();
        tags.add("tags1");
        tags.add("tags2");
        tags.add("tags3");
        DockerClient dockerClient = newDockerClient();
        dockerClient.buildImageCmd()
                .withDockerfile(new File(dockerfilePath))
//                .withDockerfilePath(dockerfilePath)
                .withBuildArg("args1", "value1")
                .withBuildArg("args2", "value2")
                .withBuildArg("args3", "value3")
                .withLabels(labels)
                .withTags(tags)
                .exec(new BuildImageProgressMonitor(msg -> {
                    stringBuilder.append(msg);
                    System.out.println(msg);
                    System.out.println("------------------------------------------------------------------------------------------------------------");
                })).awaitImageId();

        dockerClient.close();
        System.out.println(stringBuilder.toString());
    }

    @Test
    public void test02() throws IOException, DockerException, InterruptedException {
        DefaultDockerClient docker = DefaultDockerClient.builder()
                .uri("http://10.255.8.212:2375")
//                .registryAuthSupplier()
                .build();

//        ContainerConfig.builder().
//        docker.createContainer()

//        docker.createService(ServiceSpec.builder().)

//        docker.build()

        List<com.spotify.docker.client.messages.Container> containers = docker.listContainers();
        for (com.spotify.docker.client.messages.Container container : containers) {
            log.info(JacksonMapper.nonEmptyMapper().toJson(container));
        }
        Swarm swarm = docker.inspectSwarm();
        log.info(JacksonMapper.nonEmptyMapper().toJson(swarm));
        docker.close();
    }

//    @Test
//    public void test03() {
//        List<PortBinding> portBindings = new ArrayList<>();
//        portBindings.add(new PortBinding(Ports.Binding.bindPortRange(10000, 99999), ExposedPort.tcp(1314)));
//        CreateContainerResponse response = DockerClientUtils.createContainer("60815f5cb49d", "admin-demo:1.0.0-SNAPSHOT", portBindings, null);
//        log.info(response.toString());
//    }
}
