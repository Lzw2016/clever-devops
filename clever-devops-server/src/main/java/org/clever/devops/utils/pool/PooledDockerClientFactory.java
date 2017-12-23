package org.clever.devops.utils.pool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.clever.common.model.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;

/**
 * DockerClient 创建工厂
 * 作者： lzw<br/>
 * 创建时间：2017-12-22 11:35 <br/>
 */
@Slf4j
public class PooledDockerClientFactory implements PooledObjectFactory<DockerClient> {

    /**
     * DockerClient 连接配置信息
     */
    private final List<DockerClientConfig> clientConfigs;

    /**
     * 当前使用连接配置位置
     */
    private static Integer currentIndex = 0;

    public PooledDockerClientFactory(List<DockerClientConfig> clientConfigs) {
        if (clientConfigs == null || clientConfigs.size() <= 0) {
            throw new BusinessException("DockerClientConfig配置不能为空");
        }
        this.clientConfigs = clientConfigs;
    }

    public PooledDockerClientFactory(DockerClientConfig clientConfig) {
        if (clientConfig == null) {
            throw new BusinessException("DockerClientConfig配置不能为空");
        }
        this.clientConfigs = new ArrayList<>();
        this.clientConfigs.add(clientConfig);
    }

    /**
     * 创建连接
     */
    @Override
    public synchronized PooledObject<DockerClient> makeObject() throws Exception {
        if (currentIndex >= clientConfigs.size()) {
            currentIndex = 0;
        }
        DockerClientConfig dockerClientConfig = clientConfigs.get(currentIndex++);
        DockerClient dockerClient = DockerClientBuilder.getInstance(dockerClientConfig).build();
        PooledObject<DockerClient> p = new DefaultPooledObject<>(dockerClient);
        log.debug("新增 DockerClient -> {}", dockerClientConfig);
        return p;
    }

    /**
     * 销毁 DockerClient
     */
    @Override
    public void destroyObject(PooledObject<DockerClient> p) throws Exception {
        DockerClient client = p.getObject();
        client.close();
        log.debug("关闭 DockerClient");
    }

    /**
     * 验证 DockerClient 是否可用
     */
    @Override
    public boolean validateObject(PooledObject<DockerClient> p) {
        DockerClient client = p.getObject();
        try {
            client.pingCmd().exec();
        } catch (Throwable e) {
            log.warn("DockerClient 不可使用", e);
            return false;
        }
        return true;
    }

    /**
     * 激活 DockerClient
     */
    @Override
    public void activateObject(PooledObject<DockerClient> p) throws Exception {
        DockerClient client = p.getObject();
        client.pingCmd().exec();
    }

    /**
     * 休眠 DockerClient
     */
    @Override
    public void passivateObject(PooledObject<DockerClient> p) throws Exception {

    }
}
