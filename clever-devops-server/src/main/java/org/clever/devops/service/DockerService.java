package org.clever.devops.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.server.service.BaseService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Docker 服务
 */
@Service
@Slf4j
public class DockerService extends BaseService {
    public void test01() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.166.130:2375")
//                .withDockerTlsVerify(true)
//                .withDockerCertPath("/home/user/.docker/certs")
//                .withDockerConfig("/home/user/.docker")
                .withApiVersion("1.33")
//                .withRegistryUrl("https://index.docker.io/v1/")
//                .withRegistryUsername("dockeruser")
//                .withRegistryPassword("ilovedocker")
//                .withRegistryEmail("dockeruser@github.com")
                .build();

//        // using jaxrs/jersey implementation here (netty impl is also available)
//        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
//                .withReadTimeout(1000)
//                .withConnectTimeout(1000)
//                .withMaxTotalConnections(100)
//                .withMaxPerRouteConnections(10);
//
//        DockerClient dockerClient = DockerClientBuilder.getInstance(config)
//                .withDockerCmdExecFactory(dockerCmdExecFactory)
//                .build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        Info info = dockerClient.infoCmd().exec();
        log.info(info.toString());


        dockerClient.close();
    }

    public void test02() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.166.130:2375")
                .withApiVersion("1.33")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        List<SearchItem> dockerSearch = dockerClient.searchImagesCmd("redis").exec();
        log.info(dockerSearch.toString());

        dockerClient.close();
    }

    public void test03() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.166.130:2375")
                .withApiVersion("1.33")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        PullImageResultCallback pullImageResultCallback = dockerClient.pullImageCmd("mongo").withTag("3.4.10").exec(new PullImageResultCallback());
        pullImageResultCallback.awaitSuccess();

        dockerClient.close();
    }

    public void test04() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.166.130:2375")
                .withApiVersion("1.33")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        List<Image> list = dockerClient.listImagesCmd().exec();
        for (Image image : list) {
            log.info(image.toString());
        }
        dockerClient.close();
    }

    public void test05() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.166.130:2375")
                .withApiVersion("1.33")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        BuildImageResultCallback callback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                log.info(item.toString());
                super.onNext(item);
            }
        };

        Set<String> tags = new HashSet<>();
        tags.add("admin-demo:1.0.0");
        String imageId = dockerClient.buildImageCmd(new File("E:\\Source\\admin-demo"))
                .withTags(tags)
                .exec(callback).awaitImageId();

        log.info(imageId);
        dockerClient.close();
    }

    public void test06() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.166.130:2375")
                .withApiVersion("1.33")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        dockerClient.removeImageCmd("c3be1781e815").exec();

        dockerClient.close();
    }

    public void test07() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.166.130:2375")
                .withApiVersion("1.33")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        CreateContainerResponse response = dockerClient.createContainerCmd("admin-demo:1.0.0")
                .withName("admin-demo-v1.0.0-001")
                .withPortBindings(new PortBinding(new Ports.Binding(null, "8080"), new ExposedPort(1314)))
                .exec();
        log.info(response.toString());

        dockerClient.startContainerCmd(response.getId()).exec();


        dockerClient.close();
    }

}
