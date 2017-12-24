package org.clever.devops.websocket;

import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.config.GlobalConfig;
import org.clever.devops.dto.response.BuildImageRes;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.ImageConfigMapper;
import org.clever.devops.utils.CodeRepositoryUtils;
import org.clever.devops.utils.ConsoleOutput;
import org.clever.devops.utils.ImageConfigUtils;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

/**
 * 构建镜像的任务处理类
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-06 10:12 <br/>
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class BuildImageTask extends Thread {

    @Autowired
    private GlobalConfig globalConfig;
    @Autowired
    private ImageConfigMapper imageConfigMapper;

    /**
     * 连接当前任务的Session集合
     */
    private ConcurrentSet<WebSocketSession> sessionSet = new ConcurrentSet<>();

    /**
     * 记录所有输出日志
     */
    private StringBuilder allLogText = new StringBuilder();

    /**
     * 响应发送数据
     */
    private BuildImageRes buildImageRes = new BuildImageRes();

    /**
     * 当前操作的“代码仓库”
     */
    private CodeRepository codeRepository;

    /**
     * 当前操作的“Docker镜像配置”
     */
    private ImageConfig imageConfig;

    /**
     * 新建一个 BuildImageTask
     *
     * @param session        WebSocket连接
     * @param codeRepository 当前操作的“代码仓库”
     * @param imageConfig    当前操作的“Docker镜像配置”
     */
    public static BuildImageTask newBuildImageTask(WebSocketSession session, CodeRepository codeRepository, ImageConfig imageConfig) {
        BuildImageTask buildImageTask = SpringContextHolder.getBean(BuildImageTask.class);
        buildImageTask.init(session, codeRepository, imageConfig);
        return buildImageTask;
    }

    /**
     * 初始化 BuildImageTask
     *
     * @param session        WebSocket连接
     * @param codeRepository 当前操作的“代码仓库”
     * @param imageConfig    当前操作的“Docker镜像配置”
     */
    private void init(WebSocketSession session, CodeRepository codeRepository, ImageConfig imageConfig) {
        sessionSet.add(session);
        this.codeRepository = codeRepository;
        this.imageConfig = imageConfig;
        // 构建响应数据
        buildImageRes.setImageConfigId(imageConfig.getId());
    }

    /**
     * 增加一个WebSocketSession到当前任务
     */
    public void addWebSocketSession(WebSocketSession session) {
        BuildImageRes tmp = new BuildImageRes();
        buildImageRes.setImageConfigId(imageConfig.getId());
        tmp.setStartTime(buildImageRes.getStartTime());
        tmp.setLogText(allLogText.toString());
        tmp.setComplete(false);
        sendMessage(session, tmp);
        sessionSet.add(session);
    }

    /**
     * 从当前任务移除一个WebSocketSession
     *
     * @param sessionId SessionID
     */
    public boolean removeWebSocketSession(String sessionId) {
        WebSocketSession rm = sessionSet.stream().filter(session -> Objects.equals(session.getId(), sessionId)).findFirst().orElse(null);
        return rm != null && sessionSet.remove(rm);
    }

    /**
     * 返回连接当前任务的Session数量
     */
    public int getWebSocketSessionSize() {
        return sessionSet == null ? 0 : sessionSet.size();
    }

    /**
     * 返回当前任务构建的“镜像配置ID”
     */
    public Long getImageConfigId() {
        return imageConfig.getId();
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
            // 更新 ImageConfig 状态
            ImageConfig updateImageConfig = new ImageConfig();
            updateImageConfig.setId(imageConfig.getId());
            updateImageConfig.setBuildState(ImageConfig.buildState_1);
            updateImageConfig.setBuildStartTime(new Date());
            updateImageConfig.setUpdateDate(new Date());
            imageConfigMapper.updateByPrimaryKeySelective(updateImageConfig);
            // 1.下载代码
            downloadCode();
            // 2.编译代码
            compileCode();
            // 3.构建镜像
            buildImage();
            // 4.清除临时文件
            clearTmpFile();
            // 镜像构建成功
            buildState = ImageConfig.buildState_S;
            sendCompleteMessage("------------------------------------------------------------- 镜像构建成功 -------------------------------------------------------------");
        } catch (Throwable e) {
            buildState = ImageConfig.buildState_F;
            sendLogText(String.format("镜像构建失败，错误原因: %1$s", e.getMessage()));
            sendCompleteMessage("------------------------------------------------------------- 镜像构建失败 -------------------------------------------------------------");
            log.error("镜像构建失败", e);
        } finally {
            ImageConfig updateImageConfig = new ImageConfig();
            updateImageConfig.setId(imageConfig.getId());
            updateImageConfig.setBuildState(buildState);
            updateImageConfig.setBuildEndTime(new Date());
            updateImageConfig.setBuildLogs(allLogText.toString());
            updateImageConfig.setUpdateDate(new Date());
            imageConfigMapper.updateByPrimaryKeySelective(updateImageConfig);
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
        sendLogText("------------------------------------------------------------- 1.下载代码 -------------------------------------------------------------");
        // 删除之前下载的代码文件
        if (CodeRepositoryUtils.deleteCode(imageConfig)) {
            sendLogText("[1.下载代码] 上一次构建镜像下载的代码文件删除成功");
        } else {
            sendLogText("[1.下载代码] 上一次构建镜像下载的代码文件删除失败");
        }
        // 更新 -- 代码下载临时文件夹路径
        ImageConfig updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setCodeDownloadPath(FilenameUtils.concat(globalConfig.getCodeDownloadPath(), UUID.randomUUID().toString()));
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(updateImageConfig);
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
        sendLogText(String.format("[1.下载代码] 更新Branch的最新的commitId [ %1$s -> %2$s ]", imageConfig.getBranch(), imageConfig.getCommitId()));
        // 更新CommitID
        updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setCommitId(commitId);
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(updateImageConfig);
        // 下载代码
        CodeRepositoryUtils.downloadCode(codeRepository, imageConfig, this::sendConsoleLogText);
        sendLogText("[1.下载代码] 完成");
    }

    /**
     * 2.编译代码
     */
    private void compileCode() {
        // 验证代码编译类型
        if (!Objects.equals(ImageConfig.buildType_Maven, imageConfig.getBuildType())) {
            throw new BusinessException("暂时只支持使用Maven编译");
        }
        sendLogText("------------------------------------------------------------- 2.编译代码 -------------------------------------------------------------");
        // 更新 -- ImageConfig 编译状态
        ImageConfig updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setBuildState(ImageConfig.buildState_2);
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(updateImageConfig);
        // 编译代码
        CodeRepositoryUtils.compileCode(imageConfig, new ConsoleOutput() {
            @Override
            public void output(String str) {
                sendConsoleLogText(str);
            }

            @Override
            public void completed() {
                sendConsoleLogText("\n[2.编译代码] 编译完成\n");
            }
        });
    }

    /**
     * 3.构建镜像
     */
    private void buildImage() {
        sendLogText("------------------------------------------------------------- 3.构建镜像 -------------------------------------------------------------");
        // 更新 -- ImageConfig 编译状态
        ImageConfig updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setBuildState(ImageConfig.buildState_3);
        updateImageConfig.setUpdateDate(new Date());
        updateImageConfig.setId(imageConfig.getId());
        imageConfigMapper.updateByPrimaryKeySelective(updateImageConfig);
        // 构建镜像
        String imageId = ImageConfigUtils.buildImage(codeRepository, imageConfig, new BuildImageProgressMonitor(this::sendConsoleLogText));
        // 更新 -- ImageConfig 镜像ID
        updateImageConfig = new ImageConfig();
        updateImageConfig.setId(imageConfig.getId());
        updateImageConfig.setImageId(imageId);
        updateImageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(updateImageConfig);
    }

    /**
     * 4.清除临时文件
     */
    private void clearTmpFile() {
        sendLogText("------------------------------------------------------------- 4.清除临时文件 -------------------------------------------------------------");
        // 删除下载的代码
        if (CodeRepositoryUtils.deleteCode(imageConfig)) {
            sendLogText("[4.清除临时文件] 删除下载的代码成功");
        } else {
            sendLogText("[4.清除临时文件] 删除下载的代码失败");
        }
    }

    /**
     * 发送控制台输出到所有的客户端 (处理“\b”、“\r”字符)
     *
     * @param str 控制台输出
     */
    private void sendConsoleLogText(String str) {
        if (str == null) {
            return;
        }
        // 统一换行处理
        str = str.replace("\r\n", "\n");
        // 处理控制台控制字符
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '\b':
                    allLogText.deleteCharAt(allLogText.length() - 1);
                    break;
                case '\r':
                    int start = allLogText.lastIndexOf("\n") + 1;
                    allLogText.delete(start, allLogText.length());
                    break;
                default:
                    allLogText.append(ch);
            }
        }
        // 输出数据
        buildImageRes.setLogText(str);
        buildImageRes.setComplete(false);
        sendMessage(buildImageRes);
    }

    /**
     * 发送日志消息到所有的客户端
     *
     * @param logText 日志消息
     */
    private void sendLogText(String logText) {
        logText = String.format("%1$s\n", StringUtils.trim(logText));
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
    private void sendCompleteMessage(String completeMessage) {
        completeMessage = String.format("%1$s\n", StringUtils.trim(completeMessage));
        allLogText.append(completeMessage);
        buildImageRes.setLogText(completeMessage);
        buildImageRes.setComplete(true);
        // 发送消息
        sendMessage(buildImageRes);
        // 关闭所有连接
        for (WebSocketSession session : sessionSet) {
            WebSocketCloseSessionUtils.closeSession(session);
        }
    }

    /**
     * 发送消息到所有的客户端
     *
     * @param buildImageRes 消息对象
     */
    private void sendMessage(BuildImageRes buildImageRes) {
        Set<WebSocketSession> rmSet = new HashSet<>();
        for (WebSocketSession session : sessionSet) {
            if (!session.isOpen()) {
                rmSet.add(session);
                continue;
            }
            sendMessage(session, buildImageRes);
        }
        // 移除关闭了的Session
        sessionSet.removeAll(rmSet);
    }

    /**
     * 发送消息到指定的客户端
     *
     * @param session       WebSocket连接
     * @param buildImageRes 消息对象
     */
    private void sendMessage(WebSocketSession session, BuildImageRes buildImageRes) {
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(buildImageRes));
        try {
            session.sendMessage(textMessage);
        } catch (Throwable e) {
            log.error("[BuildImageTask] 发送任务结束消息异常", e);
        }
    }
}
