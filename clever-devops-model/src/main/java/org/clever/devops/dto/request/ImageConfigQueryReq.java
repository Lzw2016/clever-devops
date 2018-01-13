package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.QueryByPage;

/**
 * 查询Docker镜像配置
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-03 15:34 <br/>
 */
@ApiModel("查询Docker镜像配置")
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageConfigQueryReq extends QueryByPage {

    /**
     * 项目名称
     */
    @ApiModelProperty("项目名称")
    private String projectName;

    /**
     * 项目语言(如 Java Node Go PHP)
     */
    @ApiModelProperty("项目语言(如 Java Node Go PHP)")
    private String language;

    /**
     * 代码仓库地址
     */
    @ApiModelProperty("代码仓库地址")
    private String repositoryUrl;

    /**
     * 代码仓库版本管理方式(如 GIT SVN)
     */
    @ApiModelProperty("代码仓库版本管理方式(如 GIT SVN)")
    private String repositoryType;

    /**
     * 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     */
    @ApiModelProperty("代码仓库授权类型(0：不需要授权；1：用户名密码；)")
    private String authorizationType;

    /**
     * 代码仓库ID
     */
    @ApiModelProperty("代码仓库ID")
    private Long repositoryId;

    /**
     * 代码branch或Tag
     */
    @ApiModelProperty("代码branch或Tag")
    private String branch;

    /**
     * 代码提交ID(commitID)
     */
    @ApiModelProperty("代码提交ID(commitID)")
    private String commitId;

    /**
     * 代码编译方式(Maven npm go)
     */
    @ApiModelProperty("代码编译方式(Maven npm go)")
    private String buildType;

    /**
     * 代码编译命令(例如 mvn clean install)
     */
    @ApiModelProperty("代码编译命令(例如 mvn clean install)")
    private String buildCmd;

    /**
     * 服务需要的端口号(多个用“,”分隔)
     */
    @ApiModelProperty("服务需要的端口号(多个用“,”分隔)")
    private String serverPorts;

    /**
     * 服务访问域名
     */
    @ApiModelProperty("服务访问域名")
    private String serverUrl;
}
