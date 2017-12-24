package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 查看Docker容器日志请求
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-24 19:15 <br/>
 */
@ApiModel("查看Docker容器日志")
@EqualsAndHashCode(callSuper = true)
@Data
public class CatContainerLogReq extends BaseRequest {

    /**
     * Docker容器ID
     */
    @ApiModelProperty("Docker容器ID")
    @NotBlank
    private String containerId;
}
