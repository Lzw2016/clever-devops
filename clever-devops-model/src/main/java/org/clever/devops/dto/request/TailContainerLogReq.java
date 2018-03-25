package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-12 14:10 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TailContainerLogReq extends BaseRequest {

    /**
     * Docker容器ID
     */
    @ApiModelProperty("Docker容器ID")
    @NotBlank
    private String containerId;

//    private Boolean follow = true;

    /**
     * 是否显示容器时间
     */
    @ApiModelProperty("是否显示容器时间")
    private Boolean timestamps = false;

    /**
     * 显示容器输出流
     */
    @ApiModelProperty("显示容器输出流")
    private Boolean stdout = true;

    /**
     * 显示容器错误流
     */
    @ApiModelProperty("显示容器错误流")
    private Boolean stderr = false;
}
