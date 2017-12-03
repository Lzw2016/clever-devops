package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.devops.entity.ImageConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.util.Collection;
import java.util.Objects;

/**
 * Git操作
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 16:14 <br/>
 */
@Slf4j
public class GitUtils {

    /**
     * 获取代码仓库分支信息<br/>
     * 失败抛出异常
     *
     * @param repositoryUrl 代码仓库地址
     * @param heads         是否返回branch信息
     * @param tags          是否返回tag信息
     */
    private static Collection<Ref> getBranch(String repositoryUrl, boolean heads, boolean tags) {
        try {
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(repositoryUrl)
                    .setHeads(heads)
                    .setTags(tags)
                    .call();
            log.info("连接代码仓库成功, url={} refsSize={}", repositoryUrl, refs.size());
            return refs;
        } catch (Throwable e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * 获取代码仓库分支信息<br/>
     * 失败抛出异常
     *
     * @param repositoryUrl 代码仓库地址
     * @param username      用户名
     * @param password      密码
     * @param heads         是否返回branch信息
     * @param tags          是否返回tag信息
     */
    private static Collection<Ref> getBranch(String repositoryUrl, String username, String password, boolean heads, boolean tags) {
        try {
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(repositoryUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .setHeads(heads)
                    .setTags(tags)
                    .call();
            log.info("连接代码仓库成功, url={} refsSize={}", repositoryUrl, refs.size());
            return refs;
        } catch (Throwable e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * 测试访问代码仓库地址 <br/>
     * 连接失败抛出异常
     *
     * @param repositoryUrl 代码仓库地址
     */
    public static void testConnect(String repositoryUrl) {
        try {
            getBranch(repositoryUrl, true, false);
        } catch (Throwable e) {
            throw new BusinessException("连接代码仓库失败", e);
        }
    }

    /**
     * 测试访问代码仓库地址 (使用 用户名密码) <br/>
     * 连接失败抛出异常
     *
     * @param repositoryUrl 代码仓库地址
     * @param username      用户名
     * @param password      密码
     */
    public static void testConnect(String repositoryUrl, String username, String password) {
        try {
            getBranch(repositoryUrl, username, password, true, false);
        } catch (Throwable e) {
            throw new BusinessException("连接代码仓库失败", e);
        }
    }

    /**
     * 获取“branch或Tag”信息
     *
     * @param repositoryUrl 代码仓库地址
     * @param branch        branch或Tag
     */
    public static ImageConfig.GitBranch getBranch(String repositoryUrl, String branch) {
        Collection<Ref> refs = getBranch(repositoryUrl, true, true);
        for (Ref ref : refs) {
            if (Objects.equals(ref.getName(), branch)) {
                return new ImageConfig.GitBranch(ref.getObjectId().getName(), ref.getName());
            }
        }
        return null;
    }

    /**
     * 获取“branch或Tag”信息
     *
     * @param repositoryUrl 代码仓库地址
     * @param username      用户名
     * @param password      密码
     * @param branch        branch或Tag
     */
    public static ImageConfig.GitBranch getBranch(String repositoryUrl, String username, String password, String branch) {
        Collection<Ref> refs = getBranch(repositoryUrl, username, password, true, true);
        for (Ref ref : refs) {
            if (Objects.equals(ref.getName(), branch)) {
                return new ImageConfig.GitBranch(ref.getObjectId().getName(), ref.getName());
            }
        }
        return null;
    }
}


