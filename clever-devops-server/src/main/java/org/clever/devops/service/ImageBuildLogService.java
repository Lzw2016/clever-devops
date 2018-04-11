package org.clever.devops.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.clever.devops.dto.request.ImageBuildLogQueryReq;
import org.clever.devops.entity.ImageBuildLog;
import org.clever.devops.mapper.ImageBuildLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-11 11:20 <br/>
 */
@Service
public class ImageBuildLogService {

    @Autowired
    private ImageBuildLogMapper imageBuildLogMapper;

    public PageInfo<ImageBuildLog> findByPage(ImageBuildLogQueryReq imageBuildLogQueryReq) {
        return PageHelper
                .startPage(imageBuildLogQueryReq.getPageNo(), imageBuildLogQueryReq.getPageSize())
                .doSelectPageInfo(() -> imageBuildLogMapper.findByPage(imageBuildLogQueryReq));
    }

    public ImageBuildLog getImageBuildLog(Long id){
        return imageBuildLogMapper.selectByPrimaryKey(id);
    }
}
