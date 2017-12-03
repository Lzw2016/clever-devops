package org.clever.devops.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.dto.request.ImageConfigAddDto;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.service.ImageConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
