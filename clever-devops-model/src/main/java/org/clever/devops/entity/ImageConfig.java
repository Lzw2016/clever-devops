package org.clever.devops.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.entity.DataEntity;

import java.util.Date;

/**
 * 实体类，对应表image_config(Docker镜像配置)<br/>
 * <p>
 * 作者：LiZW <br/>
 * 创建时间：2017-12-02 14:59:02 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageConfig extends DataEntity {

    /**
     * 当前镜像构建状态(0：未构建)
     */
    public static final Character buildState_0 = '0';

    /**
     * 当前镜像构建状态(1：正在下载代码)
     */
    public static final Character buildState_1 = '1';

    /**
     * 当前镜像构建状态(2：正在编译代码)
     */
    public static final Character buildState_2 = '2';

    /**
     * 当前镜像构建状态(3：正在构建镜像)
     */
    public static final Character buildState_3 = '3';

    /**
     * 当前镜像构建状态(S：构建成功)
     */
    public static final Character buildState_S = 'S';
    /**
     * 当前镜像构建状态(F：构建失败)
     */
    public static final Character buildState_F = 'F';

    /**
     * 代码编译方式(Maven)
     */
    public static final String buildType_Maven = "Maven";

    /**
     * 代码编译方式(npm)
     */
    public static final String buildType_npm = "npm";

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
     * Docker镜像名称
     */
    private String imageName;

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

    /**
     * 代码branch或Tag
     */
    @Data
    public static class GitBranch implements Comparable<GitBranch> {
        /**
         * 代码提交ID(commitID)
         */
        private String commitId;

        /**
         * 代码branch或Tag
         */
        private String branch;

        public GitBranch() {
        }

        /**
         * @param commitId 代码提交ID(commitID)
         * @param branch   代码branch或Tag
         */
        public GitBranch(String commitId, String branch) {
            this.commitId = commitId;
            this.branch = branch;
        }

        @Override
        public int compareTo(GitBranch o) {
            if (o == null) {
                return -1;
            }
            if (this.branch != null && o.branch != null) {
                return this.branch.compareTo(o.branch);
            }
            if (this.branch == null && o.branch == null) {
                return 0;
            }
            if (this.branch == null) {
                return -1;
            }
            return 1;
        }
    }
}
