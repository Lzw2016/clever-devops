package org.clever.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.clever.devops.dto.request.ImageBuildLogQueryReq;
import org.clever.devops.entity.ImageBuildLog;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-11 10:39 <br/>
 */
public interface ImageBuildLogMapper extends BaseMapper<ImageBuildLog> {

    List<ImageBuildLog> findByPage(@Param("query") ImageBuildLogQueryReq imageBuildLogQueryReq, IPage page);
}
