package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-03 0:45 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CodeRepositoryUpdateReq extends BaseRequest {

    /**
     * 项目名称
     */
    @ApiModelProperty("项目名称")
    @Length(min = 1, max = 255)
    private String projectName;

    /**
     * 项目描述
     */
    @ApiModelProperty("项目描述")
    @Length(max = 2047)
    private String description;

    /**
     * 项目语言(如 Java Node Go PHP)
     */
    @ApiModelProperty("项目语言(如 Java Node Go PHP)")
    @Pattern(regexp = "Java|Node|Go|PHP")
    private String language;

    /**
     * 代码仓库地址
     */
    @ApiModelProperty("代码仓库地址")
    @Length(min = 1, max = 1023)
    private String repositoryUrl;

    /**
     * 代码仓库版本管理方式(如 GIT SVN)
     */
    @ApiModelProperty("代码仓库版本管理方式(如 GIT SVN)")
    @Pattern(regexp = "GIT|SVN")
    private String repositoryType;

    /**
     * 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     */
    @ApiModelProperty("代码仓库授权类型(0：不需要授权；1：用户名密码；)")
    @Pattern(regexp = "[01]")
    private String authorizationType;

    /**
     * 代码仓库授权信息
     */
    @ApiModelProperty("代码仓库授权信息")
    private String authorizationInfo;
}
