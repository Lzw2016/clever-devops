package org.clever.devops.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-03 20:15 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetGitBranchDto extends BaseRequest {

    /**
     * 代码仓库地址
     */
    @ApiModelProperty("代码仓库地址")
    @NotEmpty
    @Length(max = 1023)
    private String repositoryUrl;

    /**
     * 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     */
    @ApiModelProperty("代码仓库授权类型(0：不需要授权；1：用户名密码；)")
    @NotEmpty
    @Pattern(regexp = "[01]")
    private String authorizationType;

    /**
     * 代码仓库授权信息
     */
    @ApiModelProperty("代码仓库授权信息")
    private String authorizationInfo;

    /**
     * 代码branch或Tag
     */
    @ApiModelProperty("代码branch或Tag")
    @NotEmpty
    @Length(max = 63)
    private String branch;
}
