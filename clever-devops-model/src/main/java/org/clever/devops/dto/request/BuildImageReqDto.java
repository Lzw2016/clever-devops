package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;

import javax.validation.constraints.NotNull;

/**
 * 构建镜像的请求
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 13:04 <br/>
 */
@ApiModel("构建镜像")
@EqualsAndHashCode(callSuper = true)
@Data
public class BuildImageReqDto extends BaseRequest {

    /**
     * 镜像配置ID
     */
    @ApiModelProperty("镜像配置ID")
    @NotNull
    private Long imageConfigId;


}
