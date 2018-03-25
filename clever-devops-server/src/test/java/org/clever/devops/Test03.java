package org.clever.devops;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import com.spotify.docker.client.messages.swarm.*;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.codec.EncodeDecodeUtils;
import org.clever.common.utils.mapper.JacksonMapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-11 20:24 <br/>
 */
@Slf4j
public class Test03 {

    @SuppressWarnings("UnnecessaryLocalVariable")
    private DockerClient newDockerClient() {
        DockerClient docker = DefaultDockerClient.builder()
                .uri("http://192.168.159.131:2375")
//                .dockerCertificates()
//                .registryAuthSupplier()
                .apiVersion("v1.34")
//                .header()
                .connectionPoolSize(100)
                .connectTimeoutMillis(1000 * 3)
                .readTimeoutMillis(1000 * 3)
                .build();
        return docker;
    }

    @Test
    public void test01() throws DockerException, InterruptedException {
        DockerClient docker = newDockerClient();
        final List<Container> containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

//        DockerClient.ListContainersParam.allContainers(false);
//        DockerClient.ListContainersParam.containersCreatedBefore("");
//        DockerClient.ListContainersParam.containersCreatedSince("");
//        DockerClient.ListContainersParam.limitContainers(10);
//        DockerClient.ListContainersParam.withContainerSizes(true);
//        DockerClient.ListContainersParam.withExitStatus(0);
//        DockerClient.ListContainersParam.withLabel("label");
//        DockerClient.ListContainersParam.withLabel("label", "value");
//        DockerClient.ListContainersParam.withStatusCreated();
//        DockerClient.ListContainersParam.withStatusExited();
//        DockerClient.ListContainersParam.withStatusPaused();
//        DockerClient.ListContainersParam.withStatusRestarting();
//        DockerClient.ListContainersParam.withStatusRunning();
//        DockerClient.ListContainersParam.filter("", "");
//        DockerClient.ListContainersParam.create("", "");

        for (Container container : containers) {
            log.info(container.toString());
        }
        docker.close();
    }

    @Test
    public void test02() throws InterruptedException, DockerException, IOException {
        DockerClient docker = newDockerClient();
        Map<String, String> labels = new HashMap<>();
        labels.put("flag", "true");
        labels.put("q1", "v1");
        String imageId = docker.build(Paths.get("G:\\CodeDownloadPath\\loan-mall"),
                "test:0.0.1",
                "./Dockerfile",
                message -> log.info("{} | {} | {} | {}", message.id(), message.status(), message.progress(), message.stream()),
                DockerClient.BuildParam.create("labels", EncodeDecodeUtils.urlEncode(JacksonMapper.nonEmptyMapper().toJson(labels)))
        );
        docker.close();
        log.info("imageId={}", imageId);
    }

    @Test
    public void t03() throws DockerException, InterruptedException {
        DockerClient docker = newDockerClient();

        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
//        final String[] ports = {"80"};
//        for (String port : ports) {
//            List<PortBinding> hostPorts = new ArrayList<>();
//            hostPorts.add(PortBinding.of("0.0.0.0", port)); // 192.168.159.131
//            portBindings.put(port, hostPorts);
//        }

        List<PortBinding> randomPort = new ArrayList<>();
        randomPort.add(PortBinding.randomPort("0.0.0.0"));
        portBindings.put("80", randomPort);
        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        ContainerConfig.Builder builder = ContainerConfig.builder();
        builder.image("test:0.0.1");
        builder.hostConfig(hostConfig);
        builder.exposedPorts("80");
//        builder.exposedPorts("9066/tcp");
        docker.createContainer(builder.build(), "test-001");
        docker.close();
        log.info("-=========================================");
    }

    @Test
    public void t04() throws DockerException, InterruptedException {
        DockerClient docker = newDockerClient();
        Swarm swarm = docker.inspectSwarm();
        docker.close();
        log.info(" == {} ", swarm);
    }

    // Server
    @Test
    public void t05() throws DockerException, InterruptedException {
        DockerClient docker = newDockerClient();
        ServiceSpec serviceSpec = ServiceSpec.builder()
                .addLabel("Test", "test")
                .name("test")
                .mode(ServiceMode.withReplicas(1))
                .taskTemplate(TaskSpec.builder().containerSpec(ContainerSpec.builder().image("test:0.0.1").build()).build())
                // .publishMode(PortConfig.PortConfigPublishMode.HOST)
                .endpointSpec(EndpointSpec.builder().addPort(PortConfig.builder().targetPort(80).build()).build())
                .build();
        ServiceCreateResponse response = docker.createService(serviceSpec);
        docker.close();
        log.info(" == {} ", response);
    }

    @Test
    public void t06() throws DockerException, InterruptedException {
        DockerClient docker = newDockerClient();
        LogStream logStream = docker.logs(
                "3ed2b5910e9953ebeec5a9c8bdb1c493b1b9c532eeb7b52793f9003cf2a2d0c6",
                DockerClient.LogsParam.follow(true),
                DockerClient.LogsParam.stdout(true),
                DockerClient.LogsParam.stderr(false)
        );
        int i = 0;
        while (logStream.hasNext()) {
            i++;
            LogMessage logMessage = logStream.next();
            ByteBuffer byteBuffer = logMessage.content();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(byteBuffer.capacity());
//            while (byteBuffer.hasRemaining()) {
//                outputStream.write(byteBuffer.get());
//            }
            String logStr = new String(bytes);
            System.out.print(logStr);
            if (i >= 10) {
                break;
            }
        }
        log.info(" == {} close");
//        log.info(" == {}", logStream.readFully());
        logStream.close();
        docker.close();
        log.info(" == {} OK");
    }
}
