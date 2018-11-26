package org.clever.devops.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    public IPage<ImageBuildLog> findByPage(ImageBuildLogQueryReq imageBuildLogQueryReq) {
        Page<ImageBuildLog> page = new Page<>(imageBuildLogQueryReq.getPageNo(), imageBuildLogQueryReq.getPageSize());
        page.setRecords(imageBuildLogMapper.findByPage(imageBuildLogQueryReq, page));
        return page;
    }

    public ImageBuildLog getImageBuildLog(Long id) {
        return imageBuildLogMapper.selectById(id);
    }
}
