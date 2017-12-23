package org.clever.devops.utils.pool;

import com.github.dockerjava.api.DockerClient;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Docker Client池管理连接的关闭操作
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-19 14:27 <br/>
 */
public class DockerClientPool extends GenericObjectPool<DockerClient> {

    public DockerClientPool(PooledObjectFactory<DockerClient> factory) {
        super(factory);
    }

    public DockerClientPool(PooledObjectFactory<DockerClient> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }

    public DockerClientPool(PooledObjectFactory<DockerClient> factory, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }
}
