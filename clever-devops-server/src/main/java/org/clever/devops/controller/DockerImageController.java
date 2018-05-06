package org.clever.devops.controller;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageHistory;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.RemovedImage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.convert.ListImageParamConvert;
import org.clever.devops.dto.request.ImageQueryReq;
import org.clever.devops.dto.request.ImageRemoveReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 17:16 <br/>
 */
@Api(description = "Docker Image操作")
@RequestMapping("/api/devops")
@RestController
public class DockerImageController extends BaseController {

    @Autowired
    private DockerClient dockerClient;

    @ApiOperation("查询Docker Image")
    @GetMapping("/docker/image" + JSON_SUFFIX)
    public List<Image> listImage(ImageQueryReq req) throws DockerException, InterruptedException {
        return dockerClient.listImages(ListImageParamConvert.convert(req));
    }

    @ApiOperation("检查Docker Image")
    @GetMapping("/docker/image/{image}" + JSON_SUFFIX)
    public ImageInfo inspect(@PathVariable String image) throws DockerException, InterruptedException {
        return dockerClient.inspectImage(image);
    }

    @ApiOperation("查询Docker ImageHistory")
    @GetMapping("/docker/image/{image}/history" + JSON_SUFFIX)
    public List<ImageHistory> history(@PathVariable String image) throws DockerException, InterruptedException {
        return dockerClient.history(image);
    }

    @ApiOperation("删除Docker镜像")
    @DeleteMapping("/docker/image/{image}" + JSON_SUFFIX)
    public List<RemovedImage> remove(@PathVariable String image, ImageRemoveReq req) throws DockerException, InterruptedException {
        return dockerClient.removeImage(image, req.getForce(), req.getNoPrune());
    }

//    @ApiOperation("删除未使用的镜像")
//    @PostMapping("/docker/image/prune" + JSON_SUFFIX)
//    public List<RemovedImage> prune(@PathVariable String image, ImageRemoveReq req) throws DockerException, InterruptedException {
//        return dockerClient.p
//    }
}
