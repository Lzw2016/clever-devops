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
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * docker-java
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2017/11/27 11:40 <br/>
 */
@Slf4j
public class Test01 {

    private static final String dockerHost = "tcp://39.108.68.132:2375";
    private static final String version = "v1.35";

    public DockerClient newDockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withApiVersion(version)
                .build();

        return DockerClientBuilder.getInstance(config).build();
    }

//    @Test
//    public void test01() throws IOException {
//        StringBuilder stringBuilder = new StringBuilder();
//
//        String dockerfilePath = "E:\\Source\\clever-devops\\Dockerfile";
//        Map<String, String> labels = new HashMap<>();
//        labels.put("labels1", "value1");
//        labels.put("labels2", "value2");
//        labels.put("labels3", "value3");
//        Set<String> tags = new HashSet<>();
//        tags.add("tags1");
//        tags.add("tags2");
//        tags.add("tags3");
//        DockerClient dockerClient = newDockerClient();
//        dockerClient.buildImageCmd()
//                .withDockerfile(new File(dockerfilePath))
////                .withDockerfilePath(dockerfilePath)
//                .withBuildArg("args1", "value1")
//                .withBuildArg("args2", "value2")
//                .withBuildArg("args3", "value3")
//                .withLabels(labels)
//                .withTags(tags)
//                .exec(new BuildImageProgressMonitor(msg -> {
//                    stringBuilder.append(msg);
//                    System.out.println(msg);
//                    System.out.println("------------------------------------------------------------------------------------------------------------");
//                })).awaitImageId();
//
//        dockerClient.close();
//        System.out.println(stringBuilder.toString());
//    }

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
        LogContainerCmd cmd = dockerClient.logContainerCmd("c03acef49ca9c826f921cdce7ae44e77e1f57c66c9bea0c8e746811bbb40d634");
        // 是否显示容器时间
        cmd.withTimestamps(false);
        // 跟随输出
        cmd.withFollowStream(true);
        // 显示容器错误流
        cmd.withStdErr(true);
        // 显示容器输出流
        cmd.withStdOut(true);
        // 日志开始输出位置
        // cmd.withSince(0);
        // tail输出的行数
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
        Thread.sleep(1000 * 10);
        cmd.close();
        Thread.sleep(1000 * 2);
        dockerClient.close();
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
