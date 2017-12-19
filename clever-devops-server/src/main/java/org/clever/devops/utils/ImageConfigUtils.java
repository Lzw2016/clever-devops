package org.clever.devops.utils;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.clever.common.utils.DateTimeUtils;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;

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

    /**
     * 构建 Docker 镜像
     *
     * @param codeRepository 代码仓库信息
     * @param imageConfig    Docker镜像配置
     * @param callback       构建进度监控回调
     * @return 返回 ImageId
     */
    public static String buildImage(CodeRepository codeRepository, ImageConfig imageConfig, BuildImageResultCallback callback) {
        // 构建镜像
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
        return DockerClientUtils.buildImage(callback, dockerfilePath, null, labels, tags);
    }

    /**
     * 新建一个 Docker 容器
     *
     * @param imageConfig Docker镜像配置
     * @return 返回 ImageId
     */
    public static CreateContainerResponse createContainer(ImageConfig imageConfig) {
        String image = imageConfig.getImageId();
        String name = String.format("%1$s-%2$s", imageConfig.getServerUrl(), DateTimeUtils.formatToString(new Date(), "yyyyMMddHHmmss"));
        Ports ports = null;
        if (StringUtils.isNotBlank(imageConfig.getServerPorts())) {
            ports = new Ports();
            String[] portArray = imageConfig.getServerPorts().split(",");
            for (String port : portArray) {
                ports.bind(new ExposedPort(NumberUtils.toInt(port)), null);
            }
        }
        return DockerClientUtils.createContainer(image, name, ports, null);
    }
}
