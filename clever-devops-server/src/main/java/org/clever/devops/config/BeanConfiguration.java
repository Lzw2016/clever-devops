package org.clever.devops.config;

import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.extension.plugins.SqlExplainInterceptor;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 10:37 <br/>
 */
@Configuration
@Slf4j
public class BeanConfiguration {

    private final GlobalConfig globalConfig;
    private DockerClient dockerClient;

    @Autowired
    public BeanConfiguration(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @Bean
    public DockerClient getDockerClient() {
        if (dockerClient != null) {
            return dockerClient;
        }
        DefaultDockerClient.Builder builder = DefaultDockerClient.builder().uri(globalConfig.getDockerUri());
        if (StringUtils.isNotBlank(globalConfig.getDockerVersion())) {
            builder.apiVersion(globalConfig.getDockerVersion());
        }
        if (globalConfig.getDockerConnectionPoolSize() != null) {
            builder.connectionPoolSize(globalConfig.getDockerConnectionPoolSize());
        }
        if (globalConfig.getDockerConnectTimeoutMillis() != null) {
            builder.connectTimeoutMillis(globalConfig.getDockerConnectTimeoutMillis());
        }
        if (globalConfig.getDockerReadTimeoutMillis() != null) {
            builder.readTimeoutMillis(globalConfig.getDockerReadTimeoutMillis());
        }
        if (StringUtils.isNotBlank(globalConfig.getDockerCertBasePath())) {
            try {
                // 复制Docker TLS认证文件到当前目录下
                File baseFile = ResourceUtils.getFile(globalConfig.getDockerCertBasePath());
                ClassPathResource classPathResource = new ClassPathResource(FilenameUtils.concat(globalConfig.getDockerCertBasePath(), globalConfig.getDockerCaCertName()));
                log.info("### 复制Docker TLS认证文件 [{}] -> [{}]", classPathResource.getPath(), baseFile.getAbsolutePath());
                FileUtils.writeByteArrayToFile(new File(FilenameUtils.concat(baseFile.getAbsolutePath(), globalConfig.getDockerCaCertName())), FileCopyUtils.copyToByteArray(classPathResource.getInputStream()));
                classPathResource = new ClassPathResource(FilenameUtils.concat(globalConfig.getDockerCertBasePath(), globalConfig.getDockerKeyName()));
                log.info("### 复制Docker TLS认证文件 [{}] -> [{}]", classPathResource.getPath(), baseFile.getAbsolutePath());
                FileUtils.writeByteArrayToFile(new File(FilenameUtils.concat(baseFile.getAbsolutePath(), globalConfig.getDockerKeyName())), FileCopyUtils.copyToByteArray(classPathResource.getInputStream()));
                classPathResource = new ClassPathResource(FilenameUtils.concat(globalConfig.getDockerCertBasePath(), globalConfig.getDockerCertName()));
                log.info("### 复制Docker TLS认证文件 [{}] -> [{}]", classPathResource.getPath(), baseFile.getAbsolutePath());
                FileUtils.writeByteArrayToFile(new File(FilenameUtils.concat(baseFile.getAbsolutePath(), globalConfig.getDockerCertName())), FileCopyUtils.copyToByteArray(classPathResource.getInputStream()));

                // 加载Docker TLS认证文件
                DockerCertificatesStore dockerCertificates = DockerCertificates
                        .builder()
                        .caCertPath(Paths.get(baseFile.getAbsolutePath(), globalConfig.getDockerCaCertName()))
                        .clientKeyPath(Paths.get(baseFile.getAbsolutePath(), globalConfig.getDockerKeyName()))
                        .clientCertPath(Paths.get(baseFile.getAbsolutePath(), globalConfig.getDockerCertName()))
                        .build().orNull();
                builder.dockerCertificates(dockerCertificates);
            } catch (DockerCertificateException e) {
                log.error("加载Docker TLS认证文件异常", e);
            } catch (FileNotFoundException e) {
                log.error("Docker TLS认证文件不存在", e);
            } catch (Throwable e) {
                log.error("初始化DockerClient失败", e);
            }
        }
        dockerClient = builder.build();
        return dockerClient;
    }

    /**
     * 分页插件
     */
    @Bean
    protected PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
//        paginationInterceptor.setSqlParser()
//        paginationInterceptor.setDialectClazz()
        paginationInterceptor.setOverflow(false);
//        paginationInterceptor.setProperties();
        return paginationInterceptor;
    }

    /**
     * 乐观锁插件<br />
     * 取出记录时，获取当前version <br />
     * 更新时，带上这个version <br />
     * 执行更新时， set version = yourVersion+1 where version = yourVersion <br />
     * 如果version不对，就更新失败 <br />
     */
    @Bean
    protected OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

//    /**
//     * 逻辑删除<br />
//     */
//    @Bean
//    public ISqlInjector sqlInjector() {
//        return new LogicSqlInjector();
//    }

    /**
     * SQL执行效率插件
     */
    @Bean
    @Profile({"dev", "test"})
    protected PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        performanceInterceptor.setFormat(false);
        performanceInterceptor.setWriteInLog(false);
        performanceInterceptor.setMaxTime(1000);
        return performanceInterceptor;
    }

    /**
     * 执行分析插件<br />
     * SQL 执行分析拦截器【 目前只支持 MYSQL-5.6.3 以上版本 】
     * 作用是分析 处理 DELETE UPDATE 语句
     * 防止小白或者恶意 delete update 全表操作！
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Bean
    @Profile({"dev", "test"})
    protected SqlExplainInterceptor sqlExplainInterceptor() {
        SqlExplainInterceptor sqlExplainInterceptor = new SqlExplainInterceptor();
//        sqlExplainInterceptor.stopProceed
        return sqlExplainInterceptor;
    }
}
