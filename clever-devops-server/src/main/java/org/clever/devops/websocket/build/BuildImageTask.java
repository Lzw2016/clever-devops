package org.clever.devops.websocket.build;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.exception.BusinessException;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.config.GlobalConfig;
import org.clever.devops.dto.request.BuildImageReq;
import org.clever.devops.dto.response.BuildImageRes;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageBuildLog;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.ImageBuildLogMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.clever.devops.utils.CodeRepositoryUtils;
import org.clever.devops.utils.ConsoleOutput;
import org.clever.devops.utils.ImageConfigUtils;
import org.clever.devops.websocket.Task;
import org.clever.devops.websocket.TaskType;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * 构建镜像的任务处理类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-06 10:12 <br/>
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class BuildImageTask extends Task {

    @Autowired
    private GlobalConfig globalConfig;
    @Autowired
    private ImageConfigMapper imageConfigMapper;
    @Autowired
    private ImageBuildLogMapper imageBuildLogMapper;

    /**
     * 记录所有输出日志
     */
    private StringBuilder allLogText = new StringBuilder();

    private BuildImageReq buildImageReq;
    private BuildImageRes buildImageRes = new BuildImageRes();
    private CodeRepository codeRepository;
    private ImageConfig imageConfig;
    private ImageBuildLog imageBuildLog;

    /**
     * 返回当前任务ID
     */
    public static String getTaskId(ImageConfig imageConfig) {
        if (imageConfig == null || imageConfig.getId() == null) {
            throw new BusinessException("生成TaskId失败");
        }
        return "BuildImageTask-" + String.valueOf(imageConfig.getId());
    }

    /**
     * 新建一个 BuildImageTask
     *
     * @param session        WebSocket连接
     * @param buildImageReq  任务请求参数
     * @param codeRepository 当前操作的“代码仓库”
     * @param imageConfig    当前操作的“Docker镜像配置”
     */
    public static BuildImageTask newBuildImageTask(WebSocketSession session, BuildImageReq buildImageReq, CodeRepository codeRepository, ImageConfig imageConfig) {
        BuildImageTask buildImageTask = SpringContextHolder.getBean(BuildImageTask.class);
        buildImageTask.init(session, buildImageReq, codeRepository, imageConfig);
        return buildImageTask;
    }

    /**
     * 初始化 BuildImageTask
     *
     * @param session        WebSocket连接
     * @param buildImageReq  任务请求参数
     * @param codeRepository 当前操作的“代码仓库”
     * @param imageConfig    当前操作的“Docker镜像配置”
     */
    private void init(WebSocketSession session, BuildImageReq buildImageReq, CodeRepository codeRepository, ImageConfig imageConfig) {
        sessionSet.add(session);
        this.buildImageReq = buildImageReq;
        this.codeRepository = codeRepository;
        this.imageConfig = imageConfig;
        // 构建响应数据
        buildImageRes.setImageConfigId(imageConfig.getId());
        // 初始化构建日志
        imageBuildLog = new ImageBuildLog();
        imageBuildLog.setRepositoryId(this.codeRepository.getId());
        imageBuildLog.setImageConfigId(this.imageConfig.getId());
        imageBuildLog.setProjectName(this.codeRepository.getProjectName());
        imageBuildLog.setRepositoryUrl(this.codeRepository.getRepositoryUrl());
        imageBuildLog.setBranch(this.imageConfig.getBranch());
        imageBuildLog.setBuildType(this.imageConfig.getBuildType());
        imageBuildLog.setBuildCmd(this.imageConfig.getBuildCmd());
        imageBuildLog.setDockerFilePath(this.imageConfig.getDockerFilePath());
        imageBuildLog.setServerPorts(this.imageConfig.getServerPorts());
        imageBuildLog.setServerUrl(this.imageConfig.getServerUrl());
        imageBuildLog.setServerCount(this.imageConfig.getServerCount());
        imageBuildLog.setBuildState(ImageConfig.buildState_0);
        imageBuildLog.setBuildStartTime(new Date());
        imageBuildLog.setCreateDate(new Date());
        imageBuildLog.setCreateBy("");
    }

    /**
     * 增加一个WebSocketSession到当前任务
     */
    @Override
    public void addWebSocketSession(WebSocketSession session) {
        BuildImageRes tmp = new BuildImageRes();
        buildImageRes.setImageConfigId(imageConfig.getId());
        tmp.setStartTime(buildImageRes.getStartTime());
        tmp.setLogText(allLogText.toString());
        tmp.setComplete(false);
        tmp.setBuildState(imageConfig.getBuildState());
        sendMessage(session, tmp);
        sessionSet.add(session);
    }

    /**
     * 返回当前任务ID
     */
    @Override
    public String getTaskId() {
        return getTaskId(imageConfig);
    }

    /**
     * 释放任务
     */
    @Override
    public void destroyTask() {
        closeAllSession();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.BuildImageTask;
    }

    /**
     * 构建镜像任务 <br/>
     * 1.下载代码 <br/>
     * 2.编译代码 <br/>
     * 3.构建镜像 <br/>
     * 4.清除临时文件 <br/>
     */
//    @Transactional(propagation = Propagation.NEVER)
    @Override
    public void run() {
        Character buildState = ImageConfig.buildState_F;
        try {
            // 设置 -- 开始构建时的时间戳
            buildImageRes.setStartTime(System.currentTimeMillis());
            // 保存构建日志
            imageBuildLog.setBuildState(ImageConfig.buildState_1);
            imageBuildLog.setBuildStartTime(new Date());
            imageBuildLogMapper.insert(imageBuildLog);
            // 更新 ImageConfig 状态
            ImageConfig updateImageConfig = new ImageConfig();
            updateImageConfig.setId(imageConfig.getId());
            updateImageConfig.setBuildState(ImageConfig.buildState_1);
            updateImageConfig.setBuildStartTime(new Date());
            updateImageConfig.setUpdateDate(new Date());
            imageConfigMapper.updateById(updateImageConfig);
            // 1.下载代码
            buildImageRes.setBuildState(ImageConfig.buildState_1);
            downloadCode();
            Thread.sleep(100);
            sendLogText(Ansi.ansi().newline().newline().reset().toString());
            // 2.编译代码
            buildImageRes.setBuildState(ImageConfig.buildState_2);
            compileCode();
            Thread.sleep(100);
            sendLogText(Ansi.ansi().newline().newline().reset().toString());
            // 3.构建镜像
            buildImageRes.setBuildState(ImageConfig.buildState_3);
            buildImage();
            Thread.sleep(100);
            sendLogText(Ansi.ansi().newline().newline().reset().toString());
            // 4.清除临时文件
            clearTmpFile();
            Thread.sleep(100);
            sendLogText(Ansi.ansi().newline().newline().reset().toString());
            // 镜像构建成功
            buildState = ImageConfig.buildState_S;
            buildImageRes.setBuildState(ImageConfig.buildState_S);
            sendLogText("------------ 镜像构建成功 ------------", Ansi.Color.BLUE);
            sendCompleteMessage(String.format("镜像ID: [%1$s]", imageConfig.getImageId()), Ansi.Color.GREEN);
        } catch (Throwable e) {
            buildState = ImageConfig.buildState_F;
            buildImageRes.setBuildState(ImageConfig.buildState_F);
            sendLogText(String.format("镜像构建失败，错误原因: %1$s", e.getMessage()), Ansi.Color.RED);
            sendLogText(String.format("具体异常堆栈: %1$s", ExceptionUtils.getStackTraceAsString(e)), Ansi.Color.RED);
            sendCompleteMessage("------------ 镜像构建失败 ------------", Ansi.Color.RED);
            log.error("镜像构建失败", e);
        } finally {
            ImageConfig updateImageConfig = new ImageConfig();
            updateImageConfig.setId(imageConfig.getId());
            updateImageConfig.setBuildState(buildState);
            updateImageConfig.setBuildEndTime(new Date());
            updateImageConfig.setBuildLogs(allLogText.toString());
            updateImageConfig.setUpdateDate(new Date());
            imageConfigMapper.updateById(updateImageConfig);
            // 更新构建日志
            ImageBuildLog updateImageBuildLog = new ImageBuildLog();
            updateImageBuildLog.setId(imageBuildLog.getId());
            updateImageBuildLog.setBuildState(buildState);
            updateImageBuildLog.setBuildEndTime(new Date());
            updateImageBuildLog.setBuildLogs(allLogText.toString());
            updateImageBuildLog.setUpdateDate(new Date());
            imageBuildLogMapper.updateById(updateImageBuildLog);
        }
        if (buildImageReq.isStartContainer()) {
            // TODO 新建容器 启动容器
        }
    }

    /**
     * 1.下载代码
     */
    private void downloadCode() {
        // 验证代码仓库类型是否支持
        if (!Objects.equals(CodeRepository.Repository_Type_Git, codeRepository.getRepositoryType())) {
            throw new BusinessException("暂时只支持Git仓库");
        }
        sendLogText("------------ 1.下载代码 ------------", Ansi.Color.BLUE);
        // 删除之前下载的代码文件
        if (CodeRepositoryUtils.deleteCode(imageConfig)) {
            sendLogText("[1.下载代码] 上一次构建镜像下载的代码文件删除成功", Ansi.Color.GREEN);
        } else {
            sendLogText("[1.下载代码] 上一次构建镜像下载的代码文件删除失败", Ansi.Color.YELLOW);
        }
        // 更新 -- 代码下载临时文件夹路径
        ImageConfig updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setCodeDownloadPath(FilenameUtils.concat(globalConfig.getCodeDownloadPath(), UUID.randomUUID().toString()));
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateById(updateImageConfig);
        imageConfig = imageConfigMapper.selectById(imageConfig.getId());
        // 更新CommitID -> 下载代码
        String commitId = null;
        switch (codeRepository.getRepositoryType()) {
            case CodeRepository.Repository_Type_Git:
                ImageConfig.GitBranch gitBranch = CodeRepositoryUtils.getBranch(codeRepository, imageConfig.getBranch());
                if (gitBranch != null) {
                    commitId = gitBranch.getCommitId();
                }
                break;
            case CodeRepository.Repository_Type_Svn:
                break;
        }
        if (StringUtils.isBlank(commitId)) {
            throw new BusinessException("读取最新的CommitID失败");
        }
        sendLogText(String.format("[1.下载代码] 更新Branch的最新的commitId [ %1$s -> %2$s ]", imageConfig.getBranch(), imageConfig.getCommitId()), null);
        sendLogText(String.format("[1.下载代码] 下载路径[%1$s]", imageConfig.getCodeDownloadPath()), null);
        // 更新CommitID
        updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setCommitId(commitId);
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateById(updateImageConfig);
        // 下载代码
        CodeRepositoryUtils.downloadCode(codeRepository, imageConfig, this::sendLogText);
        sendLogText(Ansi.ansi().newline().a("[1.下载代码] 完成").toString(), Ansi.Color.GREEN);
        imageConfig = imageConfigMapper.selectById(imageConfig.getId());
        // 更新构建日志
        ImageBuildLog updateImageBuildLog = new ImageBuildLog();
        updateImageBuildLog.setId(imageBuildLog.getId());
        updateImageBuildLog.setCodeDownloadPath(imageConfig.getCodeDownloadPath());
        updateImageBuildLog.setCommitId(imageConfig.getCommitId());
        updateImageBuildLog.setBuildState(ImageConfig.buildState_2);
        updateImageBuildLog.setUpdateDate(new Date());
        imageBuildLogMapper.updateById(updateImageBuildLog);
    }

    /**
     * 2.编译代码
     */
    private void compileCode() {
        // 验证代码编译类型
        if (!Objects.equals(ImageConfig.buildType_Maven, imageConfig.getBuildType())) {
            throw new BusinessException("暂时只支持使用Maven编译");
        }
        sendLogText("------------ 2.编译代码 ------------", Ansi.Color.BLUE);
        // 更新 -- ImageConfig 编译状态
        ImageConfig updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setBuildState(ImageConfig.buildState_2);
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateById(updateImageConfig);
        // 编译代码
        CodeRepositoryUtils.compileCode(imageConfig, new ConsoleOutput() {
            @Override
            public void output(String str) {
                sendLogText(str);
            }

            @Override
            public void completed() {
                sendLogText("[2.编译代码] 编译完成", Ansi.Color.GREEN);
            }
        });
        imageConfig = imageConfigMapper.selectById(imageConfig.getId());
        // 更新构建日志
        ImageBuildLog updateImageBuildLog = new ImageBuildLog();
        updateImageBuildLog.setId(imageBuildLog.getId());
        updateImageBuildLog.setBuildState(ImageConfig.buildState_3);
        updateImageBuildLog.setUpdateDate(new Date());
        imageBuildLogMapper.updateById(updateImageBuildLog);
    }

    /**
     * 3.构建镜像
     */
    private void buildImage() {
        sendLogText("------------ 3.构建镜像 ------------", Ansi.Color.BLUE);
        // 更新 -- ImageConfig 编译状态
        ImageConfig updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setBuildState(ImageConfig.buildState_3);
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateById(updateImageConfig);
        // 构建镜像
        String branch = imageConfig.getBranch().substring(imageConfig.getBranch().lastIndexOf('/') + 1, imageConfig.getBranch().length());
        String imageName = String.format("%1$s:%2$s", codeRepository.getProjectName(), branch);
        String imageId = ImageConfigUtils.buildImage(imageName, codeRepository, imageConfig, new BuildImageProgressMonitor(this::sendLogText));
        // 更新 -- ImageConfig 镜像ID
        updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setImageId(imageId);
        updateImageConfig.setImageName(imageName);
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateById(updateImageConfig);
        imageConfig = imageConfigMapper.selectById(imageConfig.getId());
        // 更新构建日志
        ImageBuildLog updateImageBuildLog = new ImageBuildLog();
        updateImageBuildLog.setId(imageBuildLog.getId());
        updateImageBuildLog.setBuildState(ImageConfig.buildState_3);
        updateImageBuildLog.setImageId(imageId);
        updateImageBuildLog.setImageName(imageName);
        updateImageBuildLog.setUpdateDate(new Date());
        imageBuildLogMapper.updateById(updateImageBuildLog);
    }

    /**
     * 4.清除临时文件
     */
    private void clearTmpFile() {
        sendLogText("------------ 4.清除临时文件 ------------", Ansi.Color.BLUE);
        // 删除下载的代码
        if (CodeRepositoryUtils.deleteCode(imageConfig)) {
            sendLogText("[4.清除临时文件] 删除下载的代码成功", Ansi.Color.GREEN);
        } else {
            sendLogText("[4.清除临时文件] 删除下载的代码失败", Ansi.Color.YELLOW);
        }
    }

    /**
     * 发送日志消息到所有的客户端
     *
     * @param logText 日志消息
     */
    private void sendLogText(String logText) {
        allLogText.append(logText);
        buildImageRes.setLogText(logText);
        buildImageRes.setComplete(false);
        sendMessage(buildImageRes);
    }

    /**
     * 发送日志消息到所有的客户端
     *
     * @param logText 日志消息
     */
    private void sendLogText(String logText, Ansi.Color color) {
        Ansi ansi = Ansi.ansi();
        if (color != null) {
            ansi.fg(color);
        }
        logText = ansi.a(logText).newline().reset().toString();
        allLogText.append(logText);
        buildImageRes.setLogText(logText);
        buildImageRes.setComplete(false);
        sendMessage(buildImageRes);
    }

    /**
     * 发送任务结束消息到所有的客户端
     * 1.发送任务结束消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param completeMessage 任务结束消息
     */
    private void sendCompleteMessage(String completeMessage, Ansi.Color color) {
        Ansi ansi = Ansi.ansi();
        if (color != null) {
            ansi.fg(color);
        }
        completeMessage = ansi.a(completeMessage).newline().reset().toString();
        allLogText.append(completeMessage);
        buildImageRes.setLogText(completeMessage);
        buildImageRes.setComplete(true);
        // 发送消息
        sendMessage(buildImageRes);
        // 关闭所有连接
        closeAllSession();
    }
}
