package org.clever.devops;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.clever.devops.websocket.build.BuildImageProgressMonitor;
import org.junit.Test;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * docker-java
 * <p>
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
    public void test03() throws IOException {
        DockerClient dockerClient = newDockerClient();

        Map<String, String> labels = new HashMap<>();
        labels.put("labels001", "labels-001");
        labels.put("labels002", "labels-002");
        labels.put("labels003", "labels-003");

        Ports ports = new Ports();
        // ports.bind(new ExposedPort(1314), null); // 随机导出端口
        // ports.bind(new ExposedPort(1314), Ports.Binding.bindPort(8080)); // 指定端口 随机IP
        ports.bind(new ExposedPort(1314), Ports.Binding.bindIpAndPort("192.168.159.131", 8080)); // 指定IP端口
        CreateContainerResponse response = dockerClient.createContainerCmd("60815f5cb49d")
                .withName("admin-demo-1.0.0-SNAPSHOT")
                .withPortBindings()
                .withPortBindings(ports)
                .withPublishAllPorts(true)
                .withLabels(labels)
//                .withAliases("TEST001")
                .exec();
        dockerClient.startContainerCmd(response.getId()).exec();
        dockerClient.close();

    }

    @Test
    public void test04() throws InterruptedException, IOException {
        DockerClient dockerClient = newDockerClient();
        LogContainerCmd cmd = dockerClient.logContainerCmd("3d0e68ad8e36eb6dd27451ea0ace23b32272e3e4cca549d951f58cf78745dcfa");
        cmd.withTimestamps(false);
        cmd.withFollowStream(true);
        cmd.withStdErr(true);
        cmd.withStdOut(true);
        // cmd.withSince(0);
        cmd.withTail(1000);
        // cmd.withTailAll();
        ResultCallback resultCallback = cmd.exec(new ResultCallback<Frame>() {
            private Closeable closeable;

            @Override
            public void onStart(Closeable closeable) {
                this.closeable = closeable;
            }

            @Override
            public void onNext(Frame object) {
                log.info(new String(object.getPayload()));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("查看日志出现异常", throwable);
            }

            @Override
            public void onComplete() {
                log.info("onComplete");
            }

            @Override
            public void close() throws IOException {
                if (closeable != null) {
                    closeable.close();
                }
                log.info("close");
            }
        });

//        resultCallback.wait();

        while (true) {
            Thread.sleep(100);
        }

//        dockerClient.close();
    }

    @Test
    public void test05() throws IOException, InterruptedException {
        DockerClient dockerClient = newDockerClient();
        List<Image> imageList = dockerClient.listImagesCmd()
//                .withImageNameFilter("admin-demo:master")
                .withDanglingFilter(true)
                .exec(); // admin-demo:master
        for (Image image : imageList) {
            log.info("###### image ID ={}", image.getId());
        }
        dockerClient.close();
    }
}
