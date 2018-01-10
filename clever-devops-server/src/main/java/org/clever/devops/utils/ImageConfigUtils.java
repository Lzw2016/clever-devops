package org.clever.devops.utils;

import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.DateTimeUtils;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;

import java.io.File;
import java.util.*;

/**
 * 构建镜像工具类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-19 12:45 <br/>
 */
public class ImageConfigUtils {

    // private static final GlobalConfig GLOBAL_CONFIG = SpringContextHolder.getBean(GlobalConfig.class);

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

    private static final DockerClientUtils dockerClientUtils = SpringContextHolder.getBean(DockerClientUtils.class);

    /**
     * 构建 Docker 镜像
     *
     * @param codeRepository 代码仓库信息
     * @param imageConfig    Docker镜像配置
     * @param callback       构建进度监控回调
     * @return 返回 ImageId
     */
    public static String buildImage(CodeRepository codeRepository, ImageConfig imageConfig, BuildImageResultCallback callback) {
        // 构建镜像 - 整理参数
        Map<String, String> labels = new HashMap<>();
        labels.put(IMAGE_LABEL_PROJECT_NAME, codeRepository.getProjectName());
        labels.put(IMAGE_LABEL_LANGUAGE, codeRepository.getLanguage());
        labels.put(IMAGE_LABEL_REPOSITORY_URL, codeRepository.getRepositoryUrl());
        labels.put(IMAGE_LABEL_REPOSITORY_TYPE, codeRepository.getRepositoryType());
        labels.put(IMAGE_LABEL_BRANCH, imageConfig.getBranch());
        labels.put(IMAGE_LABEL_COMMIT_ID, imageConfig.getCommitId());
        labels.put(IMAGE_LABEL_SERVER_PORTS, imageConfig.getServerPorts());
        labels.put(IMAGE_LABEL_SERVER_URL, imageConfig.getServerUrl());
        String branch = imageConfig.getBranch();
        branch = branch.substring(branch.lastIndexOf('/') + 1, branch.length());
        Set<String> tags = new HashSet<>();
        tags.add(String.format("%1$s:%2$s", codeRepository.getProjectName(), branch));
        String dockerfilePath = FilenameUtils.concat(imageConfig.getCodeDownloadPath(), imageConfig.getDockerFilePath());
        File dockerfile = new File(dockerfilePath);
        if (!dockerfile.exists() || !dockerfile.isFile()) {
            throw new BusinessException(String.format("Dockerfile文件[%1$s]不存在", dockerfilePath));
        }
        return dockerClientUtils.execute(client -> {
            // 删除之前的镜像
            if (StringUtils.isNotBlank(imageConfig.getImageId())) {
                List<Image> imageList = client.listImagesCmd().withImageNameFilter(imageConfig.getImageId()).exec();
                for (Image image : imageList) {
                    client.removeImageCmd(image.getId())
                            .withForce(true)        // 删除镜像，即使它被停止的容器使用或被标记
                            .withNoPrune(false)     // 删除未被标记的父镜像
                            .exec();
                }
            }
            // 构建镜像
            BuildImageCmd buildImageCmd = client.buildImageCmd();
            buildImageCmd.withDockerfile(dockerfile);
            buildImageCmd.withLabels(labels);
            buildImageCmd.withTags(tags);
            return buildImageCmd.exec(callback).awaitImageId();
        });
    }

    /**
     * 新建一个 Docker 容器
     *
     * @param imageConfig Docker镜像配置
     * @return 返回 ImageId
     */
    public static CreateContainerResponse createContainer(final ImageConfig imageConfig) {
        String image = imageConfig.getImageId();
        String name = String.format("%1$s-%2$s", imageConfig.getServerUrl(), DateTimeUtils.formatToString(new Date(), "yyyyMMddHHmmss"));
        return dockerClientUtils.execute(client -> {
            CreateContainerCmd createContainerCmd = client.createContainerCmd(image);
            createContainerCmd.withName(name);
            if (StringUtils.isNotBlank(imageConfig.getServerPorts())) {
                Ports ports = new Ports();
                String[] portArray = imageConfig.getServerPorts().split(",");
                for (String port : portArray) {
                    ports.bind(new ExposedPort(NumberUtils.toInt(port)), null);
                }
                createContainerCmd.withPortBindings(ports);
                createContainerCmd.withPublishAllPorts(true);
            }
            return createContainerCmd.exec();
        });
    }
}
