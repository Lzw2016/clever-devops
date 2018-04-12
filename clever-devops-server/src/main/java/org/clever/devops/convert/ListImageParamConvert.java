package org.clever.devops.convert;

import com.spotify.docker.client.DockerClient;
import org.apache.commons.lang3.StringUtils;
import org.clever.devops.dto.request.ImageQueryReq;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-12 21:50 <br/>
 */
public class ListImageParamConvert {

    public static DockerClient.ListImagesParam[] convert(ImageQueryReq req) {
        List<DockerClient.ListImagesParam> list = new ArrayList<>();
        if (req.getAllImages() != null && req.getAllImages()) {
            list.add(DockerClient.ListImagesParam.allImages());
        }
        if (req.getDanglingImages() != null && req.getDanglingImages()) {
            list.add(DockerClient.ListImagesParam.danglingImages());
        }
        if (req.getDigests() != null && req.getDigests()) {
            list.add(DockerClient.ListImagesParam.digests());
        }
        if (StringUtils.isNotBlank(req.getName())) {
            list.add(DockerClient.ListImagesParam.byName(req.getName()));
        }
        if (req.getWithLabels() != null) {
            for (String label : req.getWithLabels()) {
                list.add(DockerClient.ListImagesParam.withLabel(label));
            }
        }
        return list.toArray(new DockerClient.ListImagesParam[list.size()]);
    }
}
