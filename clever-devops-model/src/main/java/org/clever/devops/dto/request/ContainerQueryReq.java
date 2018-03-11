package org.clever.devops.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-11 20:53 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContainerQueryReq extends BaseRequest {

    private Boolean allContainers;

    /**
     * 只显示id之前创建的容器，包括不运行的容器。
     */
    private String createdBeforeId;

    private String createdSinceId;

    private Integer limit;

    private Boolean withSizes;

    private Integer withExitStatus;

    private Boolean withStatusCreated;

    private Boolean withStatusExited;

    private Boolean withStatusPaused;

    private Boolean withStatusRestarting;

    private Boolean withStatusRunning;

    private List<String> withLabels;

//    private HashMap<String, String> withLabelMap;
//    private HashMap<String, String> filterMap;
//    private HashMap<String, String> paramMap;
}
