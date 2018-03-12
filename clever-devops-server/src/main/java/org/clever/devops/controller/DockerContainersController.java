package org.clever.devops.controller;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.TopResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.convert.ListContainersParamConvert;
import org.clever.devops.convert.LogsParamConvert;
import org.clever.devops.convert.RemoveContainerParamConvert;
import org.clever.devops.dto.request.CatContainerLogReq;
import org.clever.devops.dto.request.ContainerQueryReq;
import org.clever.devops.dto.request.ContainerRemoveReq;
import org.clever.devops.dto.response.CatContainerLogRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-17 17:20 <br/>
 */
@Api(description = "Docker Containers操作")
@RequestMapping("/devops")
@RestController
@Slf4j
public class DockerContainersController extends BaseController {

    @Autowired
    private DockerClient dockerClient;

    @ApiOperation("查询Docker Containers")
    @GetMapping("/docker/container" + JSON_SUFFIX)
    public List<Container> listContainers(ContainerQueryReq req) throws DockerException, InterruptedException {
        return dockerClient.listContainers(ListContainersParamConvert.convert(req));
    }

    @ApiOperation("检查Docker Containers")
    @GetMapping("/docker/container/{id}" + JSON_SUFFIX)
    public ContainerInfo inspect(@PathVariable String id) throws DockerException, InterruptedException {
        return dockerClient.inspectContainer(id);
    }

    @ApiOperation("查看Docker Containers内运行的进程列表")
    @GetMapping("/docker/container/{id}/top" + JSON_SUFFIX)
    public TopResults containerProcesses(@PathVariable String id, @RequestParam(required = false) String psArgs) throws DockerException, InterruptedException {
        if (StringUtils.isNotBlank(psArgs)) {
            return dockerClient.topContainer(id, psArgs);
        }
        return dockerClient.topContainer(id);
    }

    @ApiOperation("启动Docker Containers")
    @GetMapping("/docker/container/{id}/start" + JSON_SUFFIX)
    public void start(@PathVariable String id) throws DockerException, InterruptedException {
        dockerClient.startContainer(id);
    }

    @ApiOperation("停止Docker Containers")
    @GetMapping("/docker/container/{id}/stop" + JSON_SUFFIX)
    public void stop(@PathVariable String id, @RequestParam(required = false, defaultValue = "0") Integer secondsToWaitBeforeKilling) throws DockerException, InterruptedException {
        dockerClient.stopContainer(id, secondsToWaitBeforeKilling);
    }

    @ApiOperation("重新启动Docker Containers")
    @GetMapping("/docker/container/{id}/restart" + JSON_SUFFIX)
    public void restart(@PathVariable String id, @RequestParam(required = false) Integer secondsToWaitBeforeRestart) throws DockerException, InterruptedException {
        if (secondsToWaitBeforeRestart != null) {
            dockerClient.restartContainer(id, secondsToWaitBeforeRestart);
        } else {
            dockerClient.restartContainer(id);
        }
    }

    @ApiOperation("杀死Docker Containers")
    @GetMapping("/docker/container/{id}/kill" + JSON_SUFFIX)
    public void kill(@PathVariable String id) throws DockerException, InterruptedException {
        dockerClient.killContainer(id);
    }

    @ApiOperation("暂停Docker Containers")
    @GetMapping("/docker/container/{id}/pause" + JSON_SUFFIX)
    public void pause(@PathVariable String id) throws DockerException, InterruptedException {
        dockerClient.pauseContainer(id);
    }

    @ApiOperation("恢复Docker Containers(取消暂停)")
    @GetMapping("/docker/container/{id}/unpause" + JSON_SUFFIX)
    public void unpause(@PathVariable String id) throws DockerException, InterruptedException {
        dockerClient.unpauseContainer(id);
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
    public void rename(@PathVariable String id, @RequestParam("newName") String newName) throws DockerException, InterruptedException {
        dockerClient.renameContainer(id, newName);
    }

    @ApiOperation("删除Docker Containers")
    @GetMapping("/docker/container/{id}/remove" + JSON_SUFFIX)
    public void remove(@PathVariable String id, ContainerRemoveReq req) throws DockerException, InterruptedException {
        dockerClient.removeContainer(id, RemoveContainerParamConvert.convert(req));
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

    @ApiOperation("读取Docker Containers的日志")
    @GetMapping("/docker/container/logs/{id}" + JSON_SUFFIX)
    public CatContainerLogRes logs(@PathVariable String id, @Validated CatContainerLogReq catContainerLogReq) throws DockerException, InterruptedException {
        final CatContainerLogRes catContainerLogRes = new CatContainerLogRes(null, false);
        try (LogStream logStream = dockerClient.logs(id, LogsParamConvert.convert(catContainerLogReq))) {
            catContainerLogRes.setComplete(true);
            catContainerLogRes.setLogText(logStream.readFully());
        }
        return catContainerLogRes;
    }

    @ApiOperation("读取Docker 监控数据")
    @GetMapping("/docker/container/stats/{id}" + JSON_SUFFIX)
    public ContainerStats stats(@PathVariable String id) throws DockerException, InterruptedException {
        return dockerClient.stats(id);
    }
}
