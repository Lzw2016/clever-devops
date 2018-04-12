package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-11 20:53 <br/>
 */
@ApiModel("查询Docker容器")
@EqualsAndHashCode(callSuper = true)
@Data
public class ContainerQueryReq extends BaseRequest {

    @ApiModelProperty("返回所有容器(默认情况下，只显示运行的容器)")
    private Boolean allContainers;

    @ApiModelProperty("只显示ID之前创建的容器，包括不运行的容器")
    private String createdBeforeId;

    @ApiModelProperty("只显示ID创建的容器，包括不运行的容器")
    private String createdSinceId;

    @ApiModelProperty("返回最近创建的容器，包括非运行的容器")
    private Integer limit;

    @ApiModelProperty("显示容器大小")
    private Boolean withSizes;

    @ApiModelProperty("显示具有给定退出状态的已退出容器")
    private Integer withExitStatus;

    @ApiModelProperty("显示状态是“Created”的容器")
    private Boolean withStatusCreated;

    @ApiModelProperty("显示状态是“Exited”的容器")
    private Boolean withStatusExited;

    @ApiModelProperty("显示状态是“Paused”的容器")
    private Boolean withStatusPaused;

    @ApiModelProperty("显示状态是“Restarting”的容器")
    private Boolean withStatusRestarting;

    @ApiModelProperty("显示状态是“Running”的容器")
    private Boolean withStatusRunning;

    @ApiModelProperty("根据Labels标签过滤")
    private List<String> withLabels;

//    private HashMap<String, String> withLabelMap;
//    private HashMap<String, String> filterMap;
//    private HashMap<String, String> paramMap;
}
