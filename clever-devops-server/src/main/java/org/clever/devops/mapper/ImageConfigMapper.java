package org.clever.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.clever.devops.entity.ImageConfig;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:05 <br/>
 */
public interface ImageConfigMapper extends BaseMapper<ImageConfig> {

    /**
     * @param repositoryId 代码仓库ID
     */
    List<ImageConfig> getByRepositoryId(@Param("repositoryId") Long repositoryId);

    /**
     * @param repositoryId 代码仓库ID
     * @param commitId     代码提交ID(commitID)
     */
    ImageConfig getByRepositoryIdAndCommitId(@Param("repositoryId") Long repositoryId, @Param("commitId") String commitId);

    /**
     * @param serverUrl 服务访问域名
     */
    ImageConfig getByServerUrl(@Param("serverUrl") String serverUrl);

    /**
     * 查询正在构建的 ImageConfig
     *
     * @param repositoryId 代码仓库ID
     */
    int getBuildingCount(@Param("repositoryId") Long repositoryId);
}
