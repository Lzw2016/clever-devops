package org.clever.devops.websocket;

import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.spring.SpringContextHolder;
import org.clever.devops.config.GlobalConfig;
import org.clever.devops.dto.response.BuildImageResDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.clever.devops.utils.CodeCompileUtils;
import org.clever.devops.utils.GitUtils;
import org.clever.devops.utils.IConsoleOutput;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
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
    private CodeRepositoryMapper codeRepositoryMapper;
    @Autowired
    private ImageConfigMapper imageConfigMapper;

    /**
     * 连接当前任务的Session集合
     */
    private ConcurrentSet<WebSocketSession> outSessionSet = new ConcurrentSet<>();

    /**
     * 记录所有输出日志
     */
    private StringBuilder allLogText = new StringBuilder();

    /**
     * 响应发送数据
     */
    private BuildImageResDto buildImageResDto = new BuildImageResDto();

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
        outSessionSet.add(session);
        this.codeRepository = codeRepository;
        this.imageConfig = imageConfig;
        // 构建响应数据
        buildImageResDto.setCodeRepository(codeRepository);
        buildImageResDto.setImageConfig(imageConfig);
    }

    /**
     * 增加一个WebSocketSession到当前任务
     */
    public void addWebSocketSession(WebSocketSession session) {
        BuildImageResDto tmp = new BuildImageResDto();
        tmp.setCodeRepository(buildImageResDto.getCodeRepository());
        tmp.setImageConfig(buildImageResDto.getImageConfig());
        tmp.setStartTime(buildImageResDto.getStartTime());
        tmp.setLogText(allLogText.toString());
        tmp.setCompleteMsg(buildImageResDto.getCompleteMsg());
        sendMessage(session, tmp);
        outSessionSet.add(session);
    }

    /**
     * 从当前任务移除一个WebSocketSession
     *
     * @param sessionId SessionID
     */
    public boolean removeWebSocketSession(String sessionId) {
        WebSocketSession rm = outSessionSet.stream().filter(session -> Objects.equals(session.getId(), sessionId)).findFirst().orElse(null);
        return rm != null && outSessionSet.remove(rm);
    }

    /**
     * 返回连接当前任务的Session数量
     */
    public int getWebSocketSessionSize() {
        return outSessionSet == null ? 0 : outSessionSet.size();
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
        sendLogText("------------------------------------------------------------- 开始构建镜像 -------------------------------------------------------------");
        // 设置
        buildImageResDto.setStartTime(System.currentTimeMillis());
        sendLogText("------------------------------------------------------------- 1.下载代码 -------------------------------------------------------------");
        // 删除之前下载的代码
        if (StringUtils.isNotBlank(imageConfig.getCodeDownloadPath())) {
            File deleteFile = new File(imageConfig.getCodeDownloadPath());
            if (deleteFile.exists()) {
                try {
                    FileUtils.forceDelete(deleteFile);
                    sendLogText("[1.下载代码] 上一次构建镜像下载的代码文件删除成功");
                } catch (Throwable e) {
                    log.error("删除下载的代码文件失败", e);
                    sendLogText("[1.下载代码] 上一次构建镜像下载的代码文件删除失败");
                }
            }
        }
        imageConfig.setBuildState(ImageConfig.buildState_1);
        imageConfig.setCodeDownloadPath(FilenameUtils.concat(globalConfig.getCodeDownloadPath(), UUID.randomUUID().toString()));
        imageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(imageConfig);
        if (!Objects.equals(CodeRepository.Repository_Type_Git, codeRepository.getRepositoryType())) {
            sendCompleteMessage("暂时只支持Git仓库");
            return;
        }
        // 更新CommitID -> 下载代码
        BatchingProgressMonitor progressMonitor = new BatchingProgressMonitor() {

            private final long interval = 1000;

            private long lastSendLog = System.currentTimeMillis();

            @Override
            protected void onUpdate(String taskName, int workCurr) {
                sendLog(taskName, workCurr);
            }

            @Override
            protected void onEndTask(String taskName, int workCurr) {
                sendLog(taskName, workCurr);
            }

            private void sendLog(String taskName, int workCurr) {
                if (System.currentTimeMillis() - lastSendLog >= interval) {
                    lastSendLog = System.currentTimeMillis();
                    sendLogText(String.format("[1.下载代码] %1$s: %2$s", taskName, workCurr));
                }
            }

            @Override
            protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
                sendLog(taskName, workCurr, workTotal, percentDone);
            }

            @Override
            protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
                sendLog(taskName, workCurr, workTotal, percentDone);
            }

            private void sendLog(String taskName, int workCurr, int workTotal, int percentDone) {
                if (System.currentTimeMillis() - lastSendLog >= interval) {
                    lastSendLog = System.currentTimeMillis();
                    sendLogText(String.format("[1.下载代码] %1$s: %2$s%% (%3$s/%4$s)", taskName, percentDone, workCurr, workTotal));
                }
            }
        };

        if (Objects.equals(CodeRepository.Repository_Type_Git, codeRepository.getRepositoryType())) {
            // Git 代码仓库
            if (Objects.equals(CodeRepository.Authorization_Type_0, codeRepository.getAuthorizationType())) {
                ImageConfig.GitBranch gitBranch = GitUtils.getBranch(codeRepository.getRepositoryUrl(), imageConfig.getBranch());
                sendLogText(String.format("[1.下载代码] 更新Branch的最新的commitId [ %1$s -> %2$s ]", gitBranch.getBranch(), gitBranch.getCommitId()));
                imageConfig.setCommitId(gitBranch.getCommitId());
                // 不需要授权
                GitUtils.downloadCode(imageConfig.getCodeDownloadPath(), codeRepository.getRepositoryUrl(), imageConfig.getCommitId(), progressMonitor);
            } else if (Objects.equals(CodeRepository.Authorization_Type_1, codeRepository.getAuthorizationType())) {
                CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(codeRepository.getAuthorizationInfo(), CodeRepository.UserNameAndPassword.class);
                if (userNameAndPassword == null) {
                    throw new BusinessException("读取授权用户名密码失败");
                }
                ImageConfig.GitBranch gitBranch = GitUtils.getBranch(codeRepository.getRepositoryUrl(), imageConfig.getBranch(), userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
                sendLogText(String.format("[1.下载代码] 更新Branch的最新的commitId [ %1$s -> %2$s ]", gitBranch.getBranch(), gitBranch.getCommitId()));
                imageConfig.setCommitId(gitBranch.getCommitId());
                // 用户名密码
                GitUtils.downloadCode(imageConfig.getCodeDownloadPath(), codeRepository.getRepositoryUrl(), imageConfig.getCommitId(), userNameAndPassword.getUsername(), userNameAndPassword.getPassword(), progressMonitor);
            } else {
                sendCompleteMessage("不支持的代码仓库授权类型");
                return;
            }
        } else {
            sendCompleteMessage("暂时只支持Git仓库");
            return;
        }
        sendLogText("[1.下载代码] 完成");

        sendLogText("------------------------------------------------------------- 2.编译代码 -------------------------------------------------------------");
        imageConfig.setBuildState(ImageConfig.buildState_2);
        imageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(imageConfig);
        if (Objects.equals(ImageConfig.buildType_Maven, imageConfig.getBuildType())) {
            CodeCompileUtils.mvn(new IConsoleOutput() {
                                     @Override
                                     public void output(String line) {
                                         sendLogText(line);
                                     }

                                     @Override
                                     public void completed() {
                                         sendLogText("[2.编译代码] 编译完成");
                                     }
                                 },
                    imageConfig.getCodeDownloadPath(),
                    new String[]{imageConfig.getBuildCmd(), String.format("--global-settings=%1$s", globalConfig.getMavenSettingsPath())});
        } else if (Objects.equals(ImageConfig.buildType_npm, imageConfig.getBuildType())) {
            sendCompleteMessage("暂时只支持Maven编译");
            return;
        }

        // TODO 测试
        sendCompleteMessage("测试中断");

        sendLogText("------------------------------------------------------------- 3.构建镜像 -------------------------------------------------------------");
        imageConfig.setBuildState(ImageConfig.buildState_3);
        imageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(imageConfig);

        sendLogText("------------------------------------------------------------- 4.清除临时文件 -------------------------------------------------------------");
        // 删除下载的代码
        File deleteFile = new File(imageConfig.getCodeDownloadPath());
        if (deleteFile.exists()) {
            try {
                FileUtils.forceDelete(deleteFile);
                sendLogText("[4.清除临时文件] 删除下载的代码成功");
            } catch (Throwable e) {
                log.error("删除下载的代码文件失败", e);
                sendLogText("[4.清除临时文件] 删除下载的代码失败");
            }
        }

        // 发送任务结束消息
        sendCompleteMessage("------------------------------------------------------------- 镜像构建成功 -------------------------------------------------------------");
        imageConfig.setBuildState(ImageConfig.buildState_S);
        imageConfig.setBuildLogs(allLogText.toString());
        imageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(imageConfig);
    }


    private void downloadCodeByGit() {

    }


    /**
     * 发送日志消息到所有的客户端
     *
     * @param logText 日志消息
     */
    private void sendLogText(String logText) {
        logText = StringUtils.trim(logText);
        allLogText.append(logText).append("\r\n");
        buildImageResDto.setLogText(logText);
        sendMessage(buildImageResDto);
    }

    /**
     * 发送任务结束消息到所有的客户端
     * 1.发送任务结束消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param completeMessage 任务结束消息
     */
    private void sendCompleteMessage(String completeMessage) {
        completeMessage = StringUtils.trim(completeMessage);
        allLogText.append(completeMessage).append("\r\n");
        buildImageResDto.setLogText(completeMessage);
        buildImageResDto.setCompleteMsg(completeMessage);
        // 发送消息
        sendMessage(buildImageResDto);
        // 关闭所有连接
        for (WebSocketSession session : outSessionSet) {
            WebSocketCloseSessionUtils.closeSession(session);
        }
    }

    /**
     * 发送消息到所有的客户端
     *
     * @param buildImageResDto 消息对象
     */
    private void sendMessage(BuildImageResDto buildImageResDto) {
        Set<WebSocketSession> rmSet = new HashSet<>();
        for (WebSocketSession session : outSessionSet) {
            if (!session.isOpen()) {
                rmSet.add(session);
                continue;
            }
            sendMessage(session, buildImageResDto);
        }
        // 移除关闭了的Session
        outSessionSet.removeAll(rmSet);
    }

    /**
     * 发送消息到指定的客户端
     *
     * @param session          WebSocket连接
     * @param buildImageResDto 消息对象
     */
    private void sendMessage(WebSocketSession session, BuildImageResDto buildImageResDto) {
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(buildImageResDto));
        try {
            session.sendMessage(textMessage);
        } catch (Throwable e) {
            log.error("[BuildImageTask] 发送任务结束消息异常", e);
        }
    }
}
