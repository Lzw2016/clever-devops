package org.clever.devops.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.dto.request.ImageBuildLogQueryReq;
import org.clever.devops.entity.ImageBuildLog;
import org.clever.devops.service.ImageBuildLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-11 11:19 <br/>
 */
@Api(description = "Docker镜像构建日志")
@RequestMapping("/api/devops")
@RestController
public class ImageBuildLogController extends BaseController {

    @Autowired
    private ImageBuildLogService imageBuildLogService;

    @ApiOperation("查询Docker镜像构建日志")
    @GetMapping("/image_build_log" + JSON_SUFFIX)
    public PageInfo<ImageBuildLog> findByPage(ImageBuildLogQueryReq imageBuildLogQueryReq) {
        return imageBuildLogService.findByPage(imageBuildLogQueryReq);
    }

    @ApiOperation("查询Docker镜像构建日志")
    @GetMapping("/image_build_log/{id}" + JSON_SUFFIX)
    public ImageBuildLog getImageBuildLog(@PathVariable Long id) {
        return imageBuildLogService.getImageBuildLog(id);
    }
}
