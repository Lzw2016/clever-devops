package org.clever.devops.utils;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.devops.entity.ImageConfig;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private static Collection<Ref> getAllBranch(String repositoryUrl, boolean heads, boolean tags) {
        return getAllBranch(repositoryUrl, heads, tags, null, null);
    }

    /**
     * 获取代码仓库分支信息<br/>
     * 失败抛出异常
     *
     * @param repositoryUrl 代码仓库地址
     * @param heads         是否返回branch信息
     * @param tags          是否返回tag信息
     * @param username      用户名
     * @param password      密码
     */
    private static Collection<Ref> getAllBranch(String repositoryUrl, boolean heads, boolean tags, String username, String password) {
        try {
            LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository().setRemote(repositoryUrl).setHeads(heads).setTags(tags);
            if (username != null) {
                lsRemoteCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            }
            Collection<Ref> refs = lsRemoteCommand.call();
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
            getAllBranch(repositoryUrl, true, false);
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
            getAllBranch(repositoryUrl, true, false, username, password);
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
        return getBranch(repositoryUrl, branch, null, null);
    }

    /**
     * 获取“branch或Tag”信息
     *
     * @param repositoryUrl 代码仓库地址
     * @param branch        branch或Tag
     * @param username      用户名
     * @param password      密码
     */
    public static ImageConfig.GitBranch getBranch(String repositoryUrl, String branch, String username, String password) {
        Collection<Ref> refs;
        if (username == null) {
            refs = getAllBranch(repositoryUrl, true, true);
        } else {
            refs = getAllBranch(repositoryUrl, true, true, username, password);
        }
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
     */
    public static List<ImageConfig.GitBranch> getAllBranch(String repositoryUrl) {
        return getAllBranch(repositoryUrl, null, null);
    }

    /**
     * 获取“branch或Tag”信息
     *
     * @param repositoryUrl 代码仓库地址
     * @param username      用户名
     * @param password      密码
     */
    public static List<ImageConfig.GitBranch> getAllBranch(String repositoryUrl, String username, String password) {
        List<ImageConfig.GitBranch> result = new ArrayList<>();
        Collection<Ref> refs;
        if (username == null) {
            refs = getAllBranch(repositoryUrl, true, true);
        } else {
            refs = getAllBranch(repositoryUrl, true, true, username, password);
        }
        for (Ref ref : refs) {
            result.add(new ImageConfig.GitBranch(ref.getObjectId().getName(), ref.getName()));
        }
        return result;
    }

    /**
     * 下载代码到本地服务器
     *
     * @param directory       下载地址文件夹
     * @param repositoryUrl   代码仓库地址
     * @param commitId        commitID
     * @param progressMonitor 下载进度监控
     */
    public static void downloadCode(String directory, String repositoryUrl, String commitId, ProgressMonitor progressMonitor) {
        downloadCode(directory, repositoryUrl, commitId, progressMonitor, null, null);
    }

    /**
     * 下载代码到本地服务器
     *
     * @param directory       下载地址文件夹
     * @param repositoryUrl   代码仓库地址
     * @param commitId        commitID
     * @param progressMonitor 下载进度监控
     * @param username        用户名
     * @param password        密码
     */
    public static void downloadCode(String directory, String repositoryUrl, String commitId, ProgressMonitor progressMonitor, String username, String password) {
        CloneCommand cloneCommand = Git.cloneRepository().setURI(repositoryUrl).setDirectory(new File(directory));
        if (progressMonitor != null) {
            cloneCommand.setProgressMonitor(progressMonitor);
        }
        if (username != null) {
            cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        }
        try (Git git = cloneCommand.call()) {
            git.checkout().setName(commitId).call();
        } catch (Throwable e) {
            log.error("Git下载代码失败", e);
            throw new BusinessException("Git下载代码失败");
        }
    }
}


