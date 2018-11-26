package org.clever.devops.utils;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.exception.BusinessException;
import org.clever.common.utils.codec.EncodeDecodeUtils;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 构建镜像工具类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-19 12:45 <br/>
 */
@Slf4j
public class ImageConfigUtils {

    // private static final GlobalConfig GLOBAL_CONFIG = SpringContextHolder.getBean(GlobalConfig.class);

    /**
     * 使用当前 管理工具构建的镜像 标识Label
     */
    private static final String DEVOPS_FLAG = "DevopsFlag";

    /**
     * 代码仓库ID
     */
    private static final String CODE_REPOSITORY_ID = "CodeRepositoryId";

    /**
     * 镜像配置ID
     */
    private static final String IMAGE_CONFIG_ID = "ImageConfigId";

    /**
     * 镜像标签 项目名称
     */
    private static final String IMAGE_LABEL_PROJECT_NAME = "ProjectName";
    /**
     * 镜像标签 项目语言
     */
    private static final String IMAGE_LABEL_LANGUAGE = "Language";
    /**
     * 镜像标签 代码仓库地址
     */
    private static final String IMAGE_LABEL_REPOSITORY_URL = "RepositoryUrl";
    /**
     * 镜像标签 代码仓库版本管理方式
     */
    private static final String IMAGE_LABEL_REPOSITORY_TYPE = "RepositoryType";
    /**
     * 镜像标签 代码branch或Tag
     */
    private static final String IMAGE_LABEL_BRANCH = "Branch";
    /**
     * 镜像标签 代码提交ID
     */
    private static final String IMAGE_LABEL_COMMIT_ID = "CommitId";
    /**
     * 镜像标签 服务需要的端口号
     */
    private static final String IMAGE_LABEL_SERVER_PORTS = "ServerPorts";
    /**
     * 镜像标签 服务访问域名
     */
    private static final String IMAGE_LABEL_SERVER_URL = "ServerUrl";

    private static final DockerClient DOCKER_CLIENT = SpringContextHolder.getBean(DockerClient.class);

    /**
     * 构建 Docker 镜像
     *
     * @param imageName       镜像名称
     * @param codeRepository  代码仓库信息
     * @param imageConfig     Docker镜像配置
     * @param progressHandler 构建进度监控回调
     * @return 返回 ImageId
     */
    public static String buildImage(String imageName, CodeRepository codeRepository, ImageConfig imageConfig, ProgressHandler progressHandler) {
        String imageId;
        // 构建镜像 - 整理参数
        Map<String, String> labels = new HashMap<>();
        labels.put(DEVOPS_FLAG, "true");
        labels.put(CODE_REPOSITORY_ID, String.valueOf(codeRepository.getId()));
        labels.put(IMAGE_CONFIG_ID, String.valueOf(imageConfig.getId()));
        labels.put(IMAGE_LABEL_PROJECT_NAME, codeRepository.getProjectName());
        labels.put(IMAGE_LABEL_LANGUAGE, codeRepository.getLanguage());
        labels.put(IMAGE_LABEL_REPOSITORY_URL, codeRepository.getRepositoryUrl());
        labels.put(IMAGE_LABEL_REPOSITORY_TYPE, codeRepository.getRepositoryType());
        labels.put(IMAGE_LABEL_BRANCH, imageConfig.getBranch());
        labels.put(IMAGE_LABEL_COMMIT_ID, imageConfig.getCommitId());
        labels.put(IMAGE_LABEL_SERVER_PORTS, imageConfig.getServerPorts());
        labels.put(IMAGE_LABEL_SERVER_URL, imageConfig.getServerUrl());
        String dockerfilePath = FilenameUtils.concat(imageConfig.getCodeDownloadPath(), imageConfig.getDockerFilePath());
        File dockerfile = new File(dockerfilePath);
        if (!dockerfile.exists() || !dockerfile.isFile()) {
            throw new BusinessException(String.format("Dockerfile文件[%1$s]不存在", dockerfilePath));
        }
        // 构建镜像
        try {
            imageId = DOCKER_CLIENT.build(Paths.get(imageConfig.getCodeDownloadPath()),
                    imageName,
                    imageConfig.getDockerFilePath(),
                    progressHandler,
                    DockerClient.BuildParam.create("labels", EncodeDecodeUtils.urlEncode(JacksonMapper.nonEmptyMapper().toJson(labels))));
        } catch (Throwable e) {
            log.error("构建镜像失败", e);
            throw ExceptionUtils.unchecked(e);
        }
        // 删除之前的镜像
        if (StringUtils.isNotBlank(imageConfig.getImageId())) {
            try {
                // Force 删除镜像，即使它被停止的容器使用或被标记
                // NoPrune 不删除未被标记的父镜像
                DOCKER_CLIENT.removeImage(imageConfig.getImageId(), true, false);
                log.info("删除Docker Image [id={}] [ServerUrl={}]", imageConfig.getImageId(), imageConfig.getServerUrl());
            } catch (Throwable ignore) {
                log.info("删除Docker Image失败 image [id={}] [ServerUrl={}]", imageConfig.getImageId(), imageConfig.getServerUrl());
                log.info("删除Docker Image失败", ignore);
            }
        }
        if (imageId == null) {
            throw new BusinessException("构建镜像失败");
        }
        return imageId;
    }

    /**
     * 新建一个 Docker 容器
     *
     * @param containerName Docker容器名称
     * @param imageConfig   Docker镜像配置
     * @return 返回 ImageId
     */
    public static ContainerCreation createContainer(String containerName, final ImageConfig imageConfig) {
        String image = imageConfig.getImageName();
        ContainerConfig.Builder builder = ContainerConfig.builder();
        builder.image(image);
        if (StringUtils.isNotBlank(imageConfig.getServerPorts())) {
            // 设置端口映射
            String[] portArray = imageConfig.getServerPorts().split(",");
            final Map<String, List<PortBinding>> portBindings = new HashMap<>();
            for (String port : portArray) {
                if (StringUtils.isBlank(port)) {
                    continue;
                }
                List<PortBinding> randomPort = new ArrayList<>();
                randomPort.add(PortBinding.randomPort("0.0.0.0"));
                portBindings.put(port, randomPort);
            }
            HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
            builder.hostConfig(hostConfig);
            builder.exposedPorts(portArray);
        }
        try {
            return DOCKER_CLIENT.createContainer(builder.build(), containerName);
        } catch (Throwable e) {
            log.error("创建容器失败", e);
            throw ExceptionUtils.unchecked(e);
        }
    }
}
