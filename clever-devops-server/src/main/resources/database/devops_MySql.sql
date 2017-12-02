/* -------------------------------- IdEntity --------------------------------
id              bigint          NOT NULL    auto_increment          COMMENT '编号',
create_by       varchar(255)    NOT NULL                            COMMENT '创建者',
create_date     datetime(3)        NOT NULL                         COMMENT '创建时间',
update_by       varchar(255)                                        COMMENT '更新者',
update_date     datetime(3)                                         COMMENT '更新时间',
del_flag        char(1)         NOT NULL    DEFAULT '1'             COMMENT '删除标记（1：正常；2：删除；3：审核）',

PRIMARY KEY (id)

（1：正常；2：删除；3：审核）
（0：否；1：是）
（0：隐藏；1：显示）

-------------------------------- IdEntity -------------------------------- */

-- CREATE DATABASE clever-devops;

/* ====================================================================================================================
    code_repository -- 代码仓库
==================================================================================================================== */
CREATE TABLE code_repository
(
    id                      bigint          NOT NULL    auto_increment          COMMENT '编号',
    create_by               varchar(255)    NOT NULL                            COMMENT '创建者',
    create_date             datetime(3)     NOT NULL                            COMMENT '创建时间',
    update_by               varchar(255)                                        COMMENT '更新者',
    update_date             datetime(3)                                         COMMENT '更新时间',

    project_name            varchar(255)    NOT NULL    UNIQUE                  COMMENT '项目名称',
    description             varchar(2047)                                       COMMENT '项目描述',
    language                varchar(31)     NOT NULL                            COMMENT '项目语言(如 Java Node Go PHP)',
    repository_url          varchar(1023)   NOT NULL                            COMMENT '代码仓库地址',
    repository_type         varchar(31)     NOT NULL                            COMMENT '代码仓库版本管理方式(如 GIT SVN)',
    authorization_type      char(1)         NOT NULL                            COMMENT '代码仓库授权类型(0：不需要授权；1：用户名密码；)',
    authorization_info      MediumText                                          COMMENT '代码仓库授权信息',
    PRIMARY KEY (id)
) COMMENT = '代码仓库';
/*------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------*/


/* ====================================================================================================================
    image_config -- Docker镜像配置
==================================================================================================================== */
CREATE TABLE image_config
(
    id                      bigint          NOT NULL    auto_increment          COMMENT '编号',
    create_by               varchar(255)    NOT NULL                            COMMENT '创建者',
    create_date             datetime(3)     NOT NULL                            COMMENT '创建时间',
    update_by               varchar(255)                                        COMMENT '更新者',
    update_date             datetime(3)                                         COMMENT '更新时间',

    repository_id           bigint          NOT NULL                            COMMENT '代码仓库ID',
    commit_id               varchar(63)     NOT NULL                            COMMENT '代码提交ID(commitID)',
    branch                  varchar(63)                                         COMMENT '代码branch或Tag',
    code_download_path      varchar(255)                                        COMMENT '代码下载临时文件夹路径',
    build_type              varchar(31)     NOT NULL                            COMMENT '代码编译方式(Maven npm go)',
    build_cmd               varchar(2047)   NOT NULL                            COMMENT '代码编译命令(例如 mvn clean install)',
    docker_file_path        varchar(255)    NOT NULL                            COMMENT 'Dockerfile文件相对路径(默认 ./Dockerfile)',
    server_ports            varchar(255)                                        COMMENT '服务需要的端口号(多个用“,”分隔)',
    server_url              varchar(255)    NOT NULL                            COMMENT '服务访问域名',
    server_count            int             NOT NULL    DEFAULT 1               COMMENT '默认运行实例数',
    build_state             char(1)         NOT NULL    DEFAULT '0'             COMMENT '当前镜像构建状态(0：未构建, 1：正在下载代码, 2：正在编译代码, 3：正在构建镜像, S：构建成功, F：构建失败)',
    image_id                varchar(255)    NOT NULL                            COMMENT '服务访问域名',
    build_start_time        datetime(3)                                         COMMENT '镜像开始构建时间',
    build_end_time          datetime(3)                                         COMMENT '镜像结束构建时间',
    build_logs              MediumText                                          COMMENT '镜像构建日志(代码下载、编译、构建镜像日志)',
    description             varchar(2047)                                       COMMENT '镜像说明',
    PRIMARY KEY (id)
) COMMENT = 'Docker镜像配置';
/*------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------*/




/* ====================================================================================================================
    
==================================================================================================================== */
CREATE TABLE 
(
    id              bigint          NOT NULL    auto_increment          COMMENT '编号',
    create_by       varchar(255)    NOT NULL                            COMMENT '创建者',
    create_date     datetime(3)     NOT NULL                            COMMENT '创建时间',
    update_by       varchar(255)                                        COMMENT '更新者',
    update_date     datetime(3)                                         COMMENT '更新时间',


    PRIMARY KEY (id)
) COMMENT = '字典表';
/*------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------*/










/*------------------------------------------------------------------------------------------------------------------------
-- 单个Docker
Images
Containers
Networks
Volumes 
Exec 
Plugins 
System
Distribution
Logs

-- Docker Swarm
Nodes 
Services 
Tasks 
Secrets 
Configs 

-- 自动发布系统
项目代码仓库管理
项目编译发布配置
项目服务管理

--------------------------------------------------------------------------------------------------------------------------*/

































