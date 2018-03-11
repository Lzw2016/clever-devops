package org.clever.devops;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

/**
 * jgit
 *
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 17:48 <br/>
 */
@Slf4j
public class Test02 {

    private static final String remote = "https://github.com/spotify/docker-client";
    private static final String directory = "1.33";


    public void test01(String remote, String directory) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI(remote) // 设置远程仓库地址
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("", "")) // 登入验证
                .setDirectory(new File(directory))
                .setBranch("tzj.cc") // 设置分支版本
                .call();

        git.checkout()
                .setName("1cbbbf51f0") // 设置 branch 或 commitID
                .call();
        git.close();
    }


    @Test
    public void test02() throws GitAPIException {
        Collection<Ref> refs = Git.lsRemoteRepository()
                .setRemote(remote)
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("Lzw2016", ""))
                .setHeads(true)
                .setTags(true)
                .call();
        for (Ref ref : refs) {
            log.info("####　{} | {}", ref.getName(), ref.getObjectId().getName());
        }
    }

    @Test
    public void t03() {
        log.info("".getClass().getSimpleName());
        log.info("".getClass().getTypeName());
        log.info("".getClass().getCanonicalName());
    }
}
