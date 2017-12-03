package org.clever.devops.mapper;

import org.apache.ibatis.annotations.Param;
import org.clever.common.server.mapper.CustomMapper;
import org.clever.devops.entity.ImageConfig;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:05 <br/>
 */
public interface ImageConfigMapper extends CustomMapper<ImageConfig> {

    /**
     * @param repositoryId 代码仓库ID
     * @param commitId     代码提交ID(commitID)
     */
    ImageConfig getByRepositoryId(@Param("repositoryId") Long repositoryId, @Param("commitId") String commitId);
}
