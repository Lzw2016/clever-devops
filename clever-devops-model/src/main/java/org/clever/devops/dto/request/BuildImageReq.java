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
public class BuildImageReq extends BaseRequest {

//    /**
//     * 设置控制台列数(Terminal 编译代码有用)
//     */
//    @Range(min = 1, max = 500)
//    private Integer columns = 350;
//
//    /**
//     * 设置控制台行数(Terminal 编译代码有用)
//     */
//    @Range(min = 1)
//    private Integer rows = 30;

    /**
     * 镜像配置ID
     */
    @ApiModelProperty("镜像配置ID")
    @NotNull
    private Long imageConfigId;

    /**
     * 构建镜像之后是否要直接启动容器
     */
    private boolean startContainer;
}
