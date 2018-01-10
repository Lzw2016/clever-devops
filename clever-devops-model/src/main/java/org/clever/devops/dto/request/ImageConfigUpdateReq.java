package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Pattern;

/**
 * 更新Docker镜像配置
 * 作者： lzw<br/>
 * 创建时间：2017-12-03 17:44 <br/>
 */
@ApiModel("更新Docker镜像配置")
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageConfigUpdateReq extends BaseRequest {

    /**
     * 代码branch或Tag
     */
    @ApiModelProperty("代码branch或Tag")
    @Length(min = 1, max = 63)
    private String branch;

    /**
     * 代码编译方式(Maven npm go)
     */
    @ApiModelProperty("代码编译方式(Maven npm go)")
    @Pattern(regexp = "Maven|npm")
    private String buildType;

    /**
     * 代码编译命令(例如 mvn clean install)
     */
    @ApiModelProperty("代码编译命令(例如 mvn clean install)")
    @Length(min = 1, max = 2047)
    private String buildCmd;

    /**
     * Dockerfile文件相对路径(默认 ./Dockerfile)
     */
    @ApiModelProperty("Dockerfile文件相对路径(默认 ./Dockerfile)")
    @Length(min = 1, max = 255)
    private String dockerFilePath;

    /**
     * 服务需要的端口号(多个用“,”分隔)
     */
    @ApiModelProperty("服务需要的端口号(多个用“,”分隔)")
    @Length(max = 255)
    private String serverPorts;

    /**
     * 服务访问域名
     */
    @ApiModelProperty("服务访问域名")
    @Length(min = 1, max = 255)
    private String serverUrl;

    /**
     * 默认运行实例数
     */
    @ApiModelProperty("默认运行实例数")
    @Range(min = 1, max = 10)
    private Integer serverCount = 1;

    /**
     * 镜像说明
     */
    @ApiModelProperty("镜像说明")
    private String description;
}
