package org.clever.devops.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;

/**
 * Git服务<br/>
 */
@Service
@Slf4j
public class GitService {
    // TODO 下载代码
    public void test01(String remote, String directory) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI(remote) // 设置远程仓库地址
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("", "")) // 登入验证
                .setDirectory(new File(directory))
                .setBranch("tzj.cc") // 设置分支版本
                .call();

        git.checkout()
                .setName("1cbbbf51f0") // 设置 branch 或 commitID
                .call();
        git.close();
    }

    // TODO 获取版本分支信息
    public void test02(String remote) throws GitAPIException {
        Collection<Ref> refs = Git.lsRemoteRepository()
                .setRemote(remote)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("Lzw2016", ""))
                .setHeads(true)
                .setTags(true)
                .call();
        for (Ref ref : refs) {
            log.info("####　{} | {}", ref.getName(), ref.getObjectId().getName());
        }
    }
}
