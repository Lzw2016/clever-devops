package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.config.GlobalConfig;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.websocket.ProgressMonitorToWebSocket;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * 代码仓库工具类(调用者对代码仓库类型透明)
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 14:38 <br/>
 */
@SuppressWarnings("Duplicates")
@Slf4j
public class CodeRepositoryUtils {

    private static final GlobalConfig GLOBAL_CONFIG = SpringContextHolder.getBean(GlobalConfig.class);

    /**
     * 编译代码
     *
     * @param imageConfig   Docker镜像配置
     * @param consoleOutput 编译进度回调监控
     */
    public static void compileCode(ImageConfig imageConfig, ConsoleOutput consoleOutput) {
        boolean compileSuccess = false;
        switch (imageConfig.getBuildType()) {
            case ImageConfig.buildType_Maven:
                compileSuccess = CodeCompileUtils.mvn(
                        consoleOutput,
                        imageConfig.getCodeDownloadPath(),
                        new String[]{imageConfig.getBuildCmd(), String.format("--global-settings=%1$s", GLOBAL_CONFIG.getMavenSettingsPath())});
                break;
            case ImageConfig.buildType_npm:
                break;
            default:
                throw new BusinessException("不支持的代码编译类型");
        }
        if (!compileSuccess) {
            throw new BusinessException("代码编译失败");
        }
    }

    /**
     * 下载代码
     *
     * @param codeRepository             代码仓库信息
     * @param imageConfig                Docker镜像配置
     * @param progressMonitorToWebSocket 下载进度回调监控
     */
    public static void downloadCode(CodeRepository codeRepository, ImageConfig imageConfig, ProgressMonitorToWebSocket progressMonitorToWebSocket) {
        // 获取授权信息
        CodeRepository.UserNameAndPassword userNameAndPassword = null;
        if (Objects.equals(CodeRepository.Authorization_Type_1, codeRepository.getAuthorizationType())) {
            // 用户名密码
            userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(codeRepository.getAuthorizationInfo(), CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
        } else if (!Objects.equals(CodeRepository.Authorization_Type_0, codeRepository.getAuthorizationType())) {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
        // 下载代码
        switch (codeRepository.getRepositoryType()) {
            case CodeRepository.Repository_Type_Git:
                // GIT 仓库
                GitProgressMonitor gitProgressMonitor = new GitProgressMonitor(progressMonitorToWebSocket);
                if (userNameAndPassword != null) {
                    GitUtils.downloadCode(imageConfig.getCodeDownloadPath(), codeRepository.getRepositoryUrl(), imageConfig.getCommitId(), gitProgressMonitor, userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
                } else {
                    GitUtils.downloadCode(imageConfig.getCodeDownloadPath(), codeRepository.getRepositoryUrl(), imageConfig.getCommitId(), gitProgressMonitor);
                }
                break;
            case CodeRepository.Repository_Type_Svn:
                // SVN 仓库
                throw new BusinessException("暂不支持SVN代码仓库");
            default:
                throw new BusinessException("不支持的代码仓库类型");
        }
    }

    /**
     * 删除下载的代码文件
     *
     * @param imageConfig Docker镜像配置
     * @return 成功返回true
     */
    public static boolean deleteCode(ImageConfig imageConfig) {
        if (StringUtils.isNotBlank(imageConfig.getCodeDownloadPath())) {
            File deleteFile = new File(imageConfig.getCodeDownloadPath());
            if (deleteFile.exists()) {
                try {
                    FileUtils.forceDelete(deleteFile);
                } catch (Throwable e) {
                    log.error("删除下载的代码文件失败", e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取“branch或Tag”信息<br/>
     *
     * @param codeRepository 代码仓库信息
     */
    public static List<ImageConfig.GitBranch> getAllBranch(CodeRepository codeRepository) {
        if (Objects.equals(CodeRepository.Authorization_Type_0, codeRepository.getAuthorizationType())) {
            // 没有访问限制
            return GitUtils.getAllBranch(codeRepository.getRepositoryUrl());
        } else if (Objects.equals(CodeRepository.Authorization_Type_1, codeRepository.getAuthorizationType())) {
            // 需要用户名、密码访问
            CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(codeRepository.getAuthorizationInfo(), CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
            return GitUtils.getAllBranch(codeRepository.getRepositoryUrl(), userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
        } else {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
    }

    /**
     * 获取“branch或Tag”信息<br/>
     *
     * @param codeRepository 代码仓库信息
     * @param branch         branch或Tag
     */
    public static ImageConfig.GitBranch getBranch(CodeRepository codeRepository, String branch) {
        return getBranch(codeRepository.getRepositoryUrl(), branch, codeRepository.getAuthorizationType().toString(), codeRepository.getAuthorizationInfo());
    }

    /**
     * 获取“branch或Tag”信息<br/>
     *
     * @param repositoryUrl     代码仓库地址
     * @param branch            branch或Tag
     * @param authorizationType 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     * @param authorizationInfo 代码仓库授权信息
     */
    public static ImageConfig.GitBranch getBranch(String repositoryUrl, String branch, String authorizationType, String authorizationInfo) {
        if (Objects.equals(CodeRepository.Authorization_Type_0.toString(), authorizationType)) {
            // 没有访问限制
            return GitUtils.getBranch(repositoryUrl, branch);
        } else if (Objects.equals(CodeRepository.Authorization_Type_1.toString(), authorizationType)) {
            // 需要用户名、密码访问
            CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(authorizationInfo, CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
            return GitUtils.getBranch(repositoryUrl, userNameAndPassword.getUsername(), userNameAndPassword.getPassword(), branch);
        } else {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
    }

    /**
     * 测试连接代码仓库地址 (失败抛出异常)
     *
     * @param codeRepository 代码仓库信息
     */
    public static void testConnect(CodeRepository codeRepository) {
        // 获取授权信息
        CodeRepository.UserNameAndPassword userNameAndPassword = null;
        if (Objects.equals(CodeRepository.Authorization_Type_1, codeRepository.getAuthorizationType())) {
            // 用户名密码
            userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(codeRepository.getAuthorizationInfo(), CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
        } else if (!Objects.equals(CodeRepository.Authorization_Type_0, codeRepository.getAuthorizationType())) {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
        // 测试连接代码仓库地址
        switch (codeRepository.getRepositoryType()) {
            case CodeRepository.Repository_Type_Git:
                // GIT 仓库
                if (userNameAndPassword != null) {
                    GitUtils.testConnect(codeRepository.getRepositoryUrl(), userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
                } else {
                    GitUtils.testConnect(codeRepository.getRepositoryUrl());
                }
                break;
            case CodeRepository.Repository_Type_Svn:
                // SVN 仓库
                throw new BusinessException("暂不支持SVN代码仓库");
            default:
                throw new BusinessException("不支持的代码仓库类型");
        }
    }

    /**
     * 测试访问代码仓库地址 (使用 用户名密码) <br/>
     * 连接失败抛出异常
     *
     * @param repositoryUrl     代码仓库地址
     * @param authorizationType 授权类型
     * @param authorizationInfo 授权信息
     */
    public static void testConnect(String repositoryUrl, String authorizationType, String authorizationInfo) {
        if (Objects.equals(CodeRepository.Authorization_Type_1.toString(), authorizationType)) {
            // 用户名密码
            CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(authorizationInfo, CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
            GitUtils.testConnect(repositoryUrl, userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
        } else if (Objects.equals(CodeRepository.Authorization_Type_0.toString(), authorizationType)) {
            // 不需要授权
            GitUtils.testConnect(repositoryUrl, null, null);
        } else {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
    }
}
