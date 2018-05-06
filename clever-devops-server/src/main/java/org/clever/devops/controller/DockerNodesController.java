package org.clever.devops.controller;

import com.spotify.docker.client.DockerClient;
import io.swagger.annotations.Api;
import org.clever.common.server.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 17:21 <br/>
 */
@Api(description = "Docker Nodes操作")
@RequestMapping("/api/devops")
@RestController
public class DockerNodesController extends BaseController {

    @Autowired
    private DockerClient dockerClient;

}
