package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

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

    /**
     * 日志开始输出位置
     */
    @ApiModelProperty("日志开始输出位置")
    @Range(min = 0)
    private Integer since = 0;


    /**
     * tail 输出的行数
     */
    @ApiModelProperty("tail输出的行数")
    @Range(min = 1, max = 10000)
    private Integer tail = 1000;
}
