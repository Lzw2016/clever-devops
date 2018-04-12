package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-12 21:49 <br/>
 */
@ApiModel("查询Docker镜像")
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageQueryReq extends BaseRequest {

    @ApiModelProperty("显示所有镜像(默认情况下只显示中间层镜像)")
    private Boolean allImages;

    @ApiModelProperty("仅显示dangling镜像")
    private Boolean danglingImages;

    @ApiModelProperty("显示digests")
    private Boolean digests;

    @ApiModelProperty("根据名字过滤")
    private String name;

    @ApiModelProperty("根据Labels标签过滤")
    private List<String> withLabels;
}
