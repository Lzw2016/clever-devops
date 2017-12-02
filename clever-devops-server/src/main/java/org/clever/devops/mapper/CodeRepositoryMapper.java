package org.clever.devops.mapper;

import org.apache.ibatis.annotations.Param;
import org.clever.common.server.mapper.CustomMapper;
import org.clever.devops.entity.CodeRepository;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:00 <br/>
 */
public interface CodeRepositoryMapper extends CustomMapper<CodeRepository> {

    CodeRepository getByProjectName(@Param("projectName") String projectName);
}
