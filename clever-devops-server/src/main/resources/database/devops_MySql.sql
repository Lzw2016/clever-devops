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
  id                 BIGINT        NOT NULL    AUTO_INCREMENT
  COMMENT '编号',
  create_by          VARCHAR(255)  NOT NULL
  COMMENT '创建者',
  create_date        DATETIME(3)   NOT NULL
  COMMENT '创建时间',
  update_by          VARCHAR(255) COMMENT '更新者',
  update_date        DATETIME(3) COMMENT '更新时间',

  project_name       VARCHAR(255)  NOT NULL    UNIQUE
  COMMENT '项目名称',
  description        VARCHAR(2047) COMMENT '项目描述',
  language           VARCHAR(31)   NOT NULL
  COMMENT '项目语言(如 Java Node Go PHP)',
  repository_url     VARCHAR(1023) NOT NULL
  COMMENT '代码仓库地址',
  repository_type    VARCHAR(31)   NOT NULL
  COMMENT '代码仓库版本管理方式(如 GIT SVN)',
  authorization_type CHAR(1)       NOT NULL
  COMMENT '代码仓库授权类型(0：不需要授权；1：用户名密码；)',
  authorization_info MEDIUMTEXT COMMENT '代码仓库授权信息',
  PRIMARY KEY (id)
)
  COMMENT = '代码仓库';
/*------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------*/


/* ====================================================================================================================
    image_config -- Docker镜像配置
==================================================================================================================== */
CREATE TABLE image_config
(
  id                 BIGINT        NOT NULL    AUTO_INCREMENT
  COMMENT '编号',
  create_by          VARCHAR(255)  NOT NULL
  COMMENT '创建者',
  create_date        DATETIME(3)   NOT NULL
  COMMENT '创建时间',
  update_by          VARCHAR(255) COMMENT '更新者',
  update_date        DATETIME(3) COMMENT '更新时间',

  repository_id      BIGINT        NOT NULL
  COMMENT '代码仓库ID',
  commit_id          VARCHAR(63)   NOT NULL
  COMMENT '代码提交ID(commitID)',
  branch             VARCHAR(63) COMMENT '代码branch或Tag',
  code_download_path VARCHAR(255) COMMENT '代码下载临时文件夹路径',
  build_type         VARCHAR(31)   NOT NULL
  COMMENT '代码编译方式(Maven npm go)',
  build_cmd          VARCHAR(2047) NOT NULL
  COMMENT '代码编译命令(例如 mvn clean install)',
  docker_file_path   VARCHAR(255)  NOT NULL
  COMMENT 'Dockerfile文件相对路径(默认 ./Dockerfile)',
  server_ports       VARCHAR(255) COMMENT '服务需要的端口号(多个用“,”分隔)',
  server_url         VARCHAR(255)  NOT NULL    UNIQUE
  COMMENT '服务访问域名',
  server_count       INT           NOT NULL    DEFAULT 1
  COMMENT '默认运行实例数',
  build_state        CHAR(1)       NOT NULL    DEFAULT '0'
  COMMENT '当前镜像构建状态(0：未构建, 1：正在下载代码, 2：正在编译代码, 3：正在构建镜像, S：构建成功, F：构建失败)',
  image_id           VARCHAR(255) COMMENT 'Docker镜像ID',
  build_start_time   DATETIME(3) COMMENT '镜像开始构建时间',
  build_end_time     DATETIME(3) COMMENT '镜像结束构建时间',
  build_logs         MEDIUMTEXT COMMENT '镜像构建日志(代码下载、编译、构建镜像日志)',
  description        VARCHAR(2047) COMMENT '镜像说明',
  PRIMARY KEY (id)
)
  COMMENT = 'Docker镜像配置';
/*------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------*/




/* ====================================================================================================================
    image_build_log -- 镜像构建日志
==================================================================================================================== */
CREATE TABLE image_build_log
(
  id          BIGINT       NOT NULL    AUTO_INCREMENT
  COMMENT '编号',
  create_by   VARCHAR(255) NOT NULL
  COMMENT '创建者',
  create_date DATETIME(3)  NOT NULL
  COMMENT '创建时间',
  update_by   VARCHAR(255) COMMENT '更新者',
  update_date DATETIME(3) COMMENT '更新时间',


  PRIMARY KEY (id)
)
  COMMENT = '镜像构建日志';
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

































