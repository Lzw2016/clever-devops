package org.clever.devops.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.response.BaseResponse;

import java.util.Date;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-13 10:10 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageConfigQueryRes extends BaseResponse {

    /**
     * 创建者
     */
    protected String createBy;
    /**
     * 创建日期
     */
    protected Date createDate;
    /**
     * 更新者
     */
    protected String updateBy;
    /**
     * 更新日期
     */
    protected Date updateDate;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目语言(如 Java NodeJS Go PHP)
     */
    private String language;

    // ------------------------------------------------------------------------------ ImageConfig
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
    private Long id;
    /**
     * 代码仓库ID
     */
    private Long repositoryId;
    /**
     * 代码提交ID(commitID)
     */
    private String commitId;
    /**
     * 代码branch或Tag
     */
    private String branch;
    /**
     * 代码下载临时文件夹路径
     */
    private String codeDownloadPath;
    /**
     * 代码编译方式(Maven npm go)
     */
    private String buildType;
    /**
     * 代码编译命令(例如 mvn clean install)
     */
    private String buildCmd;
    /**
     * Dockerfile文件相对路径(默认 ./Dockerfile)
     */
    private String dockerFilePath;
    /**
     * 服务需要的端口号(多个用“,”分隔)
     */
    private String serverPorts;
    /**
     * 服务访问域名
     */
    private String serverUrl;
    /**
     * 默认运行实例数
     */
    private Integer serverCount;
    /**
     * 当前镜像构建状态(0：未构建, 1：正在下载代码, 2：正在编译代码, 3：正在构建镜像, S：构建成功, F：构建失败)
     */
    private Character buildState;
    /**
     * Docker镜像ID
     */
    private String imageId;
    /**
     * 镜像开始构建时间
     */
    private Date buildStartTime;
    /**
     * 镜像结束构建时间
     */
    private Date buildEndTime;
    /**
     * 镜像构建日志(代码下载、编译、构建镜像日志)
     */
    private String buildLogs;
    /**
     * 镜像说明
     */
    private String description;
}
