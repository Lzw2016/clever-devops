package org.clever.devops.convert;

import com.spotify.docker.client.DockerClient;
import org.clever.devops.dto.request.ContainerRemoveReq;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-12 13:40 <br/>
 */
public class RemoveContainerParamConvert {

    public static DockerClient.RemoveContainerParam[] convert(ContainerRemoveReq req) {
        List<DockerClient.RemoveContainerParam> list = new ArrayList<>();
        if (req.getForceKill() != null && req.getForceKill()) {
            list.add(DockerClient.RemoveContainerParam.forceKill());
        }
        if (req.getRemoveVolumes() != null && req.getRemoveVolumes()) {
            list.add(DockerClient.RemoveContainerParam.removeVolumes());
        }
        return list.toArray(new DockerClient.RemoveContainerParam[list.size()]);
    }

}
