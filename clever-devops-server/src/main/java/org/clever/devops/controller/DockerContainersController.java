package org.clever.devops.controller;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.utils.DockerClientUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 17:20 <br/>
 */
@Api(description = "Docker Containers操作")
@RequestMapping("/devops")
@RestController
public class DockerContainersController extends BaseController {

    @ApiOperation("查询Docker Containers")
    @GetMapping("/docker/container" + JSON_SUFFIX)
    public List<Container> listContainers() {
        return DockerClientUtils.listContainers();
    }

    @ApiOperation("新增Docker Containers")
    @PostMapping("/docker/container" + JSON_SUFFIX)
    public CreateContainerResponse createContainer() {
        List<PortBinding> portBindings = new ArrayList<>();
        portBindings.add(new PortBinding(Ports.Binding.bindIpAndPort("192.168.159.131", 28080), ExposedPort.tcp(1314)));
        return DockerClientUtils.createContainer("60815f5cb49d", "admin-demo-1.0.0-SNAPSHOT", portBindings, null);
    }
}
