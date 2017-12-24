package org.clever.devops.controller;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.TopContainerResponse;
import com.github.dockerjava.api.model.Container;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.utils.DockerClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 17:20 <br/>
 */
@Api(description = "Docker Containers操作")
@RequestMapping("/devops")
@RestController
public class DockerContainersController extends BaseController {

    @Autowired
    private DockerClientUtils dockerClientUtils;

    @ApiOperation("查询Docker Containers")
    @GetMapping("/docker/container" + JSON_SUFFIX)
    public List<Container> listContainers() {
        return dockerClientUtils.execute(client -> client.listContainersCmd().exec());
    }

    @ApiOperation("检查Docker Containers")
    @GetMapping("/docker/container/{id}" + JSON_SUFFIX)
    public InspectContainerResponse inspect(@PathVariable String id) {
        return dockerClientUtils.execute(client -> client.inspectContainerCmd(id).withSize(true).exec());
    }

    @ApiOperation("查看Docker Containers内运行的进程列表")
    @GetMapping("/docker/container/{id}/top" + JSON_SUFFIX)
    public TopContainerResponse containerProcesses(@PathVariable String id) {
        return dockerClientUtils.execute(client -> client.topContainerCmd(id).exec());
    }

    @ApiOperation("启动Docker Containers")
    @GetMapping("/docker/container/{id}/start" + JSON_SUFFIX)
    public void start(@PathVariable String id) {
        dockerClientUtils.execute(client -> client.startContainerCmd(id).exec());
    }

    @ApiOperation("停止Docker Containers")
    @GetMapping("/docker/container/{id}/stop" + JSON_SUFFIX)
    public void stop(@PathVariable String id) {
        dockerClientUtils.execute(client -> client.stopContainerCmd(id).exec());
    }

    @ApiOperation("重新启动Docker Containers")
    @GetMapping("/docker/container/{id}/restart" + JSON_SUFFIX)
    public void restart(@PathVariable String id) {
        dockerClientUtils.execute(client -> client.restartContainerCmd(id).exec());
    }

    @ApiOperation("杀死Docker Containers")
    @GetMapping("/docker/container/{id}/kill" + JSON_SUFFIX)
    public void kill(@PathVariable String id) {
        dockerClientUtils.execute(client -> client.killContainerCmd(id).exec());
    }

    @ApiOperation("暂停Docker Containers")
    @GetMapping("/docker/container/{id}/pause" + JSON_SUFFIX)
    public void pause(@PathVariable String id) {
        dockerClientUtils.execute(client -> client.pauseContainerCmd(id).exec());
    }

    @ApiOperation("恢复Docker Containers(取消暂停)")
    @GetMapping("/docker/container/{id}/unpause" + JSON_SUFFIX)
    public void unpause(@PathVariable String id) {
        dockerClientUtils.execute(client -> client.unpauseContainerCmd(id).exec());
    }

//    @ApiOperation("更新Docker Containers")
//    @GetMapping("/docker/container/{id}/update" + JSON_SUFFIX)
//    public UpdateContainerResponse update(@PathVariable String id) {
//        return dockerClientUtils.execute(client -> {
//            UpdateContainerCmd cmd = client.updateContainerCmd(id);
//            // cmd.withMemory();
//            return cmd.exec();
//        });
//    }

    @ApiOperation("重命名Docker Containers")
    @GetMapping("/docker/container/{id}/rename" + JSON_SUFFIX)
    public void rename(@PathVariable String id, @RequestParam("newName") String newName) {
        dockerClientUtils.execute(client -> client.renameContainerCmd(id).withName(newName).exec());
    }

    @ApiOperation("删除Docker Containers")
    @GetMapping("/docker/container/{id}/remove" + JSON_SUFFIX)
    public void remove(@PathVariable String id) {
        dockerClientUtils.execute(client -> {
            RemoveContainerCmd cmd = client.removeContainerCmd(id);
            cmd.withForce(false);
            cmd.withRemoveVolumes(true);
            return cmd.exec();
        });
    }

//    @ApiOperation("读取Docker Containers中文件流")
//    @GetMapping("/docker/container/{id}/archive" + JSON_SUFFIX)
//    public void archive(@PathVariable String id, @RequestParam("newName") String newName) {
//        dockerClientUtils.execute(client -> {
//            CopyArchiveToContainerCmd cmd =client.copyArchiveToContainerCmd(id);
//
//            return cmd.exec();
//        });
//    }

    // 容器日志 /containers/{id}/logs

    // 监控统计数据 /containers/{id}/stats
}
