package org.clever.devops.mapper;

import org.clever.common.server.mapper.CustomMapper;
import org.clever.devops.dto.request.ImageBuildLogQueryReq;
import org.clever.devops.entity.ImageBuildLog;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-11 10:39 <br/>
 */
public interface ImageBuildLogMapper extends CustomMapper<ImageBuildLog> {

    List<ImageBuildLog> findByPage(ImageBuildLogQueryReq imageBuildLogQueryReq);
}
