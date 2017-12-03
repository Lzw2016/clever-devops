package org.clever.devops.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.dto.request.ImageConfigAddDto;
import org.clever.devops.dto.request.ImageConfigQueryDto;
import org.clever.devops.dto.request.ImageConfigUpdateDto;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.service.ImageConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:19 <br/>
 */
@Api(description = "Docker镜像配置")
@RequestMapping("/devops")
@RestController
public class ImageConfigController extends BaseController {

    @Autowired
    private ImageConfigService imageConfigService;

    @ApiOperation("新增Docker镜像配置")
    @PostMapping("/image_config" + JSON_SUFFIX)
    public ImageConfig addImageConfig(@RequestBody @Validated ImageConfigAddDto imageConfigAddDto) {
        return imageConfigService.addImageConfig(imageConfigAddDto);
    }

    @ApiOperation("查询Docker镜像配置")
    @GetMapping("/image_config" + JSON_SUFFIX)
    public PageInfo<ImageConfig> findImageConfig(ImageConfigQueryDto imageConfigQueryDto) {
        return imageConfigService.findImageConfig(imageConfigQueryDto);
    }

    @ApiOperation("获取Docker镜像配置")
    @GetMapping("/image_config/{serverUrl}" + JSON_SUFFIX)
    public ImageConfig getImageConfig(@PathVariable("serverUrl") String serverUrl) {
        return imageConfigService.getImageConfig(serverUrl);
    }

    @ApiOperation("更新Docker镜像配置")
    @PutMapping("/image_config/{id}" + JSON_SUFFIX)
    public ImageConfig updateImageConfig(@PathVariable("id") Long id, @RequestBody @Validated ImageConfigUpdateDto imageConfigUpdateDto) {
        return imageConfigService.updateImageConfig(id, imageConfigUpdateDto);
    }
}
