package org.clever.devops.convert;

import com.spotify.docker.client.DockerClient;
import org.clever.devops.dto.request.CatContainerLogReq;
import org.clever.devops.dto.request.TailContainerLogReq;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-12 13:50 <br/>
 */
public class LogsParamConvert {

    public static DockerClient.LogsParam[] convert(CatContainerLogReq req) {
        List<DockerClient.LogsParam> list = new ArrayList<>();
        list.add(DockerClient.LogsParam.follow(false));
        if (req.getTimestamps() != null && req.getTimestamps()) {
            list.add(DockerClient.LogsParam.timestamps());
        }
        if (req.getStdout() != null && req.getStdout()) {
            list.add(DockerClient.LogsParam.stdout());
        }
        if (req.getStderr() != null && req.getStderr()) {
            list.add(DockerClient.LogsParam.stderr());
        }
        if (req.getSince() != null) {
            list.add(DockerClient.LogsParam.since(req.getSince()));
        }
        if (req.getTail() != null) {
            list.add(DockerClient.LogsParam.tail(req.getTail()));
        }
        return list.toArray(new DockerClient.LogsParam[list.size()]);
    }

    public static DockerClient.LogsParam[] convert(TailContainerLogReq req, int tail) {
        List<DockerClient.LogsParam> list = new ArrayList<>();
        list.add(DockerClient.LogsParam.follow(true));
        if (req.getTimestamps() != null && req.getTimestamps()) {
            list.add(DockerClient.LogsParam.timestamps());
        }
        if (req.getStdout() != null && req.getStdout()) {
            list.add(DockerClient.LogsParam.stdout());
        }
        if (req.getStderr() != null && req.getStderr()) {
            list.add(DockerClient.LogsParam.stderr());
        }
        // tail 输出的行数
        list.add(DockerClient.LogsParam.tail(tail));
        return list.toArray(new DockerClient.LogsParam[list.size()]);
    }

}
