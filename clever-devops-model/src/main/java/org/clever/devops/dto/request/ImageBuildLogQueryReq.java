package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.QueryByPage;

import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-04-11 10:56 <br/>
 */
@ApiModel("查询镜像构建日志")
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageBuildLogQueryReq extends QueryByPage {

    @ApiModelProperty("代码仓库ID")
    private Long repositoryId;

    @ApiModelProperty("Docker镜像配置ID")
    private Long imageConfigId;

    @ApiModelProperty("项目名称(模糊匹配)")
    private String projectName;

    @ApiModelProperty("代码仓库地址(模糊匹配)")
    private String repositoryUrl;

    @ApiModelProperty("代码提交ID(commitID)")
    private String commitId;

    @ApiModelProperty("代码branch或Tag")
    private String branch;

    @ApiModelProperty("服务需要的端口号(多个用“,”分隔)(模糊匹配)")
    private String serverPorts;

    @ApiModelProperty("服务访问域名(模糊匹配)")
    private String serverUrl;

    @ApiModelProperty("当前镜像构建状态(S：构建成功, F：构建失败)")
    @Pattern(regexp = "[SF]", message = "当前镜像构建状态(S：构建成功, F：构建失败)")
    private String buildState;

    @ApiModelProperty("Docker镜像ID")
    private String imageId;

    @ApiModelProperty("Docker镜像名称(模糊匹配)")
    private String imageName;

    @ApiModelProperty("构建开始时间-开始")
    private Date buildStartTimeStart;

    @ApiModelProperty("构建开始时间-结束")
    private Date buildStartTimeEnd;
}
