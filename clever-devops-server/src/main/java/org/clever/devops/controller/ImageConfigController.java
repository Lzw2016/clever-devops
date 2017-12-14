package org.clever.devops.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.dto.request.GetGitBranchReq;
import org.clever.devops.dto.request.ImageConfigAddReq;
import org.clever.devops.dto.request.ImageConfigQueryReq;
import org.clever.devops.dto.request.ImageConfigUpdateReq;
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
    public ImageConfig addImageConfig(@RequestBody @Validated ImageConfigAddReq imageConfigAddReq) {
        return imageConfigService.addImageConfig(imageConfigAddReq);
    }

    @ApiOperation("查询Docker镜像配置")
    @GetMapping("/image_config" + JSON_SUFFIX)
    public PageInfo<ImageConfig> findImageConfig(ImageConfigQueryReq imageConfigQueryReq) {
        return imageConfigService.findImageConfig(imageConfigQueryReq);
    }

    @ApiOperation("获取Docker镜像配置")
    @GetMapping("/image_config/{serverUrl}" + JSON_SUFFIX)
    public ImageConfig getImageConfig(@PathVariable("serverUrl") String serverUrl) {
        return imageConfigService.getImageConfig(serverUrl);
    }

    @ApiOperation("更新Docker镜像配置")
    @PutMapping("/image_config/{id}" + JSON_SUFFIX)
    public ImageConfig updateImageConfig(@PathVariable("id") Long id, @RequestBody @Validated ImageConfigUpdateReq imageConfigUpdateReq) {
        return imageConfigService.updateImageConfig(id, imageConfigUpdateReq);
    }

    @ApiOperation("删除Docker镜像配置")
    @DeleteMapping("/image_config/{id}" + JSON_SUFFIX)
    public ImageConfig delete(@PathVariable("id") Long id) {
        return imageConfigService.delete(id);
    }

    @ApiOperation("获取“branch或Tag”信息")
    @PostMapping("/git_branch" + JSON_SUFFIX)
    public ImageConfig.GitBranch getGitBranch(@RequestBody @Validated GetGitBranchReq getGitBranchReq) {
        return imageConfigService.getBranch(
                getGitBranchReq.getRepositoryUrl(),
                getGitBranchReq.getAuthorizationType(),
                getGitBranchReq.getAuthorizationInfo(),
                getGitBranchReq.getBranch());
    }

    // TODO 获取所有的 获取“branch或Tag”信息
}
