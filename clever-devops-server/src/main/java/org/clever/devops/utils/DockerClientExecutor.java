package org.clever.devops.utils;

import com.github.dockerjava.api.DockerClient;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-22 15:25 <br/>
 */
public interface DockerClientExecutor<T> {

    /**
     * 执行 Docker 操作
     */
    T execute(DockerClient client);
}
