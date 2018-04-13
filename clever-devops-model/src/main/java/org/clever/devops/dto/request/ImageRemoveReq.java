package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-13 10:34 <br/>
 */
@ApiModel("删除Docker镜像")
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageRemoveReq extends BaseRequest {

    @ApiModelProperty("删除镜像，即使它被停止的容器使用或被标记(默认 false)")
    private Boolean force = false;

    @ApiModelProperty("不删除未被标记的父镜像(默认 false)")
    private Boolean noPrune = false;
}
