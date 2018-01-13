package org.clever.devops.mapper;

import org.clever.common.server.mapper.CustomMapper;
import org.clever.devops.dto.request.ImageConfigQueryReq;
import org.clever.devops.dto.response.ImageConfigQueryRes;
import org.clever.devops.entity.CodeRepository;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-13 10:16 <br/>
 */
public interface QueryMapper extends CustomMapper<CodeRepository> {

    List<ImageConfigQueryRes> findImageConfig(ImageConfigQueryReq imageConfigQueryReq);
}
