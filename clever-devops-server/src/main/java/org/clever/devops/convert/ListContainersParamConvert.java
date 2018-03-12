package org.clever.devops.convert;

import com.spotify.docker.client.DockerClient;
import org.apache.commons.lang3.StringUtils;
import org.clever.devops.dto.request.ContainerQueryReq;

import java.util.ArrayList;
import java.util.List;


/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-12 12:28 <br/>
 */
public class ListContainersParamConvert {

    public static DockerClient.ListContainersParam[] convert(ContainerQueryReq req) {
        List<DockerClient.ListContainersParam> list = new ArrayList<>();
        if (req.getAllContainers() != null && req.getAllContainers()) {
            list.add(DockerClient.ListContainersParam.allContainers());
        }
        if (StringUtils.isNotBlank(req.getCreatedBeforeId())) {
            list.add(DockerClient.ListContainersParam.containersCreatedBefore(req.getCreatedBeforeId()));
        }
        if (StringUtils.isNotBlank(req.getCreatedSinceId())) {
            list.add(DockerClient.ListContainersParam.containersCreatedSince(req.getCreatedSinceId()));
        }
        if (req.getLimit() != null) {
            list.add(DockerClient.ListContainersParam.limitContainers(req.getLimit()));
        }
        if (req.getWithSizes() != null && req.getWithSizes()) {
            list.add(DockerClient.ListContainersParam.withContainerSizes(req.getWithSizes()));
        }
        if (req.getWithExitStatus() != null) {
            list.add(DockerClient.ListContainersParam.withExitStatus(req.getWithExitStatus()));
        }
        if (req.getWithStatusCreated() != null && req.getWithStatusCreated()) {
            list.add(DockerClient.ListContainersParam.withStatusCreated());
        }
        if (req.getWithStatusExited() != null && req.getWithStatusExited()) {
            list.add(DockerClient.ListContainersParam.withStatusExited());
        }
        if (req.getWithStatusPaused() != null && req.getWithStatusPaused()) {
            list.add(DockerClient.ListContainersParam.withStatusPaused());
        }
        if (req.getWithStatusRestarting() != null && req.getWithStatusRestarting()) {
            list.add(DockerClient.ListContainersParam.withStatusRestarting());
        }
        if (req.getWithStatusRunning() != null && req.getWithStatusRunning()) {
            list.add(DockerClient.ListContainersParam.withStatusRunning());
        }
        if (req.getWithLabels() != null) {
            for (String label : req.getWithLabels()) {
                list.add(DockerClient.ListContainersParam.withLabel(label));
            }
        }
//        DockerClient.ListContainersParam.withLabel("label");
//        DockerClient.ListContainersParam.withLabel("label", "value");
//        DockerClient.ListContainersParam.filter("", "");
//        DockerClient.ListContainersParam.create("", "");
        return list.toArray(new DockerClient.ListContainersParam[list.size()]);
    }
}
