package org.clever.devops.controller;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageHistory;
import com.spotify.docker.client.messages.ImageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.convert.ListImageParamConvert;
import org.clever.devops.dto.request.ImageQueryReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 17:16 <br/>
 */
@Api(description = "Docker Image操作")
@RequestMapping("/devops")
@RestController
public class DockerImageController extends BaseController {

    @Autowired
    private DockerClient dockerClient;

    @ApiOperation("查询Docker Image")
    @GetMapping("/docker/image" + JSON_SUFFIX)
    public List<Image> listContainers(ImageQueryReq req) throws DockerException, InterruptedException {
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
}
