package org.clever.devops.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.entity.DataEntity;

/**
 * 实体类，对应表code_repository(代码仓库)<br/>
 * <p>
 * 作者：LiZW <br/>
 * 创建时间：2017-12-02 14:56:46 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CodeRepository extends DataEntity {

    /**
     * 代码仓库授权类型(0：不需要授权；)
     */
    public static final Character Authorization_Type_0 = '0';

    /**
     * 代码仓库授权类型(1：用户名密码；)
     */
    public static final Character Authorization_Type_1 = '1';

    /**
     * 代码仓库版本管理方式 GIT
     */
    public static final String Repository_Type_Git = "GIT";

    /**
     * 代码仓库版本管理方式 SVN
     */
    public static final String Repository_Type_Svn = "SVN";


    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目语言(如 Java NodeJS Go PHP)
     */
    private String language;

    /**
     * 代码仓库地址
     */
    private String repositoryUrl;

    /**
     * 代码仓库版本管理方式(如 GIT SVN)
     */
    private String repositoryType;

    /**
     * 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     */
    private Character authorizationType;

    /**
     * 代码仓库授权信息
     */
    private String authorizationInfo;

    /**
     * 代码仓库授权信息 - 用户名密码
     */
    @Data
    public static class UserNameAndPassword {
        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;
    }
}
