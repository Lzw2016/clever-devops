package org.clever.devops.utils;

import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;

import java.util.List;
import java.util.Objects;

/**
 * 代码仓库工具类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 14:38 <br/>
 */
public class CodeRepositoryUtils {

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
        return getBranch(codeRepository.getRepositoryUrl(), codeRepository.getAuthorizationType().toString(), codeRepository.getAuthorizationInfo(), branch);
    }

    /**
     * 获取“branch或Tag”信息<br/>
     *
     * @param repositoryUrl     代码仓库地址
     * @param authorizationType 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     * @param authorizationInfo 代码仓库授权信息
     * @param branch            branch或Tag
     */
    public static ImageConfig.GitBranch getBranch(String repositoryUrl, String authorizationType, String authorizationInfo, String branch) {
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
        testConnect(codeRepository.getRepositoryUrl(), codeRepository.getAuthorizationType().toString(), codeRepository.getAuthorizationInfo());
    }

    /**
     * 测试连接代码仓库地址 (失败抛出异常)
     *
     * @param repositoryUrl     代码仓库地址
     * @param authorizationType 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     * @param authorizationInfo 代码仓库授权信息
     */
    public static void testConnect(String repositoryUrl, String authorizationType, String authorizationInfo) {
        if (Objects.equals(CodeRepository.Authorization_Type_0.toString(), authorizationType)) {
            // 没有访问限制
            GitUtils.testConnect(repositoryUrl);
        } else if (Objects.equals(CodeRepository.Authorization_Type_1.toString(), authorizationType)) {
            // 需要用户名、密码访问
            CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(authorizationInfo, CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
            GitUtils.testConnect(repositoryUrl, userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
        } else {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
    }
}
