package org.clever.devops.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.config.GlobalConfig;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Docker 操作工具类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-16 0:57 <br/>
 */
public class DockerClientUtils {

    private static final GlobalConfig GLOBAL_CONFIG = SpringContextHolder.getBean(GlobalConfig.class);

    private static final DockerClientConfig DOCKER_CLIENT_CONFIG;

    static {
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        builder.withDockerHost(GLOBAL_CONFIG.getDockerHost());
        if (StringUtils.isNotBlank(GLOBAL_CONFIG.getDockerVersion())) {
            builder.withApiVersion(GLOBAL_CONFIG.getDockerVersion());
        }
        DOCKER_CLIENT_CONFIG = builder.build();
    }

    /**
     * 新建一个DockerClient 使用完毕一定要调用close()方法关闭连接
     */
    private static DockerClient newDockerClient() {
        return DockerClientBuilder.getInstance(DOCKER_CLIENT_CONFIG).build();
    }

    /**
     * 构建 Docker 镜像
     *
     * @param callback       构建进度监控回调
     * @param dockerfilePath dockerfile文件路径
     * @param args           构建参数
     * @param labels         镜像标签(键值对)
     * @param tags           镜像Tags
     * @return 返回 ImageId
     */
    public static String buildImage(BuildImageResultCallback callback, String dockerfilePath, Map<String, String> args, Map<String, String> labels, Set<String> tags) {
        File dockerfile = new File(dockerfilePath);
        if (!dockerfile.exists() || !dockerfile.isFile()) {
            throw new BusinessException(String.format("Dockerfile文件[%1$s]不存在", dockerfilePath));
        }
        try (DockerClient dockerClient = newDockerClient()) {
            BuildImageCmd buildImageCmd = dockerClient.buildImageCmd();
            buildImageCmd.withDockerfile(dockerfile);
            if (args != null) {
                for (Map.Entry<String, String> arg : args.entrySet()) {
                    buildImageCmd.withBuildArg(arg.getKey(), arg.getValue());
                }
            }
            if (labels != null) {
                buildImageCmd.withLabels(labels);
            }
            if (tags != null) {
                buildImageCmd.withTags(tags);
            }
//            buildImageCmd.withCpusetcpus("");
//            buildImageCmd.withCpushares("");
//            buildImageCmd.withMemory(0L);
//            buildImageCmd.withMemswap(0L);
//            buildImageCmd.withCacheFrom(new HashSet<>());
//            buildImageCmd.withBuildAuthConfigs(null);
//            buildImageCmd.withForcerm(false);
//            buildImageCmd.withNoCache(false);
//            buildImageCmd.withPull(false);
//            buildImageCmd.withQuiet(false);
//            buildImageCmd.withRemove(false);
//            buildImageCmd.withShmsize(0L);
            return buildImageCmd.exec(callback).awaitImageId();
        } catch (Throwable e) {
            throw new BusinessException("构建Docker镜像失败", e);
        }
    }

    /**
     * 新建一个 Docker 容器
     *
     * @param image  镜像名称或镜像ID
     * @param name   容器名称
     * @param ports  设置绑定的端口IP
     * @param labels 容器标签
     * @return 容器信息
     */
    public static CreateContainerResponse createContainer(String image, String name, Ports ports, Map<String, String> labels) {
        try (DockerClient dockerClient = newDockerClient()) {
            CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image);
            if (name != null) {
                createContainerCmd.withName(name);
            }
            if (ports != null) {
                createContainerCmd.withPortBindings(ports);
                createContainerCmd.withPublishAllPorts(true);
            }
            if (labels != null) {
                createContainerCmd.withLabels(labels);
            }
//            createContainerCmd.withNetworkMode("ingress");
            return createContainerCmd.exec();
        } catch (Throwable e) {
            throw new BusinessException("构建Docker镜像失败", e);
        }
    }

    /**
     * 读取Docker的所有容器
     */
    public static List<Container> listContainers() {
        List<Container> result;
        try (DockerClient dockerClient = newDockerClient()) {
            result = dockerClient.listContainersCmd().exec();
        } catch (Throwable e) {
            throw new BusinessException("构建Docker镜像失败", e);
        }
        return result;
    }
}
