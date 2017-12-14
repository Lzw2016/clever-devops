package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.QueryByPage;

/**
 * 查询代码仓库
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:26 <br/>
 */
@ApiModel("查询代码仓库")
@EqualsAndHashCode(callSuper = true)
@Data
public class CodeRepositoryQueryReq extends QueryByPage {

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
}
