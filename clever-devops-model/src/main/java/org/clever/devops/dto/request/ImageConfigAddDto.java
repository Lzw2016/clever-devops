package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 新增Docker镜像配置
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-03 8:58 <br/>
 */
@ApiModel("新增Docker镜像配置")
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageConfigAddDto extends BaseRequest {

    /**
     * 代码仓库ID
     */
    @ApiModelProperty("代码仓库ID")
    @NotNull
    private Long repositoryId;

    /**
     * 代码branch或Tag
     */
    @ApiModelProperty("代码branch或Tag")
    @NotBlank
    @Length(max = 63)
    private String branch;

    /**
     * 代码编译方式(Maven npm go)
     */
    @ApiModelProperty("代码编译方式(Maven npm go)")
    @NotBlank
    @Pattern(regexp = "Maven|npm")
    private String buildType;

    /**
     * 代码编译命令(例如 mvn clean install)
     */
    @ApiModelProperty("代码编译命令(例如 mvn clean install)")
    @NotBlank
    @Length(max = 2047)
    private String buildCmd;

    /**
     * Dockerfile文件相对路径(默认 ./Dockerfile)
     */
    @ApiModelProperty("Dockerfile文件相对路径(默认 ./Dockerfile)")
    @NotBlank
    @Length(max = 255)
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
    @NotBlank
    @Length(max = 255)
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
