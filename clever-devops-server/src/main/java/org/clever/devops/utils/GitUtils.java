package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.model.exception.BusinessException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.util.Collection;

/**
 * Git操作
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 16:14 <br/>
 */
@Slf4j
public class GitUtils {

    /**
     * 测试访问代码仓库地址 <br/>
     * 连接失败抛出异常
     *
     * @param repositoryUrl 代码仓库地址
     */
    public static void testConnect(String repositoryUrl) {
        try {
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(repositoryUrl)
                    .setHeads(true)
                    .call();
            log.info("连接代码仓库成功, url={} refsSize={}", repositoryUrl, refs.size());
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
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(repositoryUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .setHeads(true)
                    .call();
            log.info("连接代码仓库成功, url={} refsSize={}", repositoryUrl, refs.size());
        } catch (Throwable e) {
            throw new BusinessException("连接代码仓库失败", e);
        }
    }
}


