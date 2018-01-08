package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-08 20:17 <br/>
 */
@ApiModel("监控Docker容器")
@EqualsAndHashCode(callSuper = true)
@Data
public class ContainerStatsReq extends BaseRequest {

    /**
     * Docker容器ID
     */
    @ApiModelProperty("Docker容器ID")
    @NotBlank
    private String containerId;
}
