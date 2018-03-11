package org.clever.devops;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

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
//                .apiVersion("1.33")
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

//    @Test
//    public void test03() {
//        List<PortBinding> portBindings = new ArrayList<>();
//        portBindings.add(new PortBinding(Ports.Binding.bindPortRange(10000, 99999), ExposedPort.tcp(1314)));
//        CreateContainerResponse response = DockerClientService.createContainer("60815f5cb49d", "admin-demo:1.0.0-SNAPSHOT", portBindings, null);
//        log.info(response.toString());
//    }
}
