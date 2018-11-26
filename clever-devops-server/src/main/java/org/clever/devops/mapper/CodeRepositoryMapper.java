package org.clever.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.clever.devops.dto.request.CodeRepositoryQueryReq;
import org.clever.devops.entity.CodeRepository;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:00 <br/>
 */
public interface CodeRepositoryMapper extends BaseMapper<CodeRepository> {

    CodeRepository getByProjectName(@Param("projectName") String projectName);

    List<CodeRepository> findCodeRepository(@Param("query") CodeRepositoryQueryReq query, IPage page);
}
