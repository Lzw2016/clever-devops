package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.validator.BaseValidatorUtils;
import org.clever.common.utils.validator.ValidatorFactoryUtils;
import org.clever.devops.config.GlobalConfig;
import org.clever.devops.dto.request.BuildImageReq;
import org.clever.devops.dto.response.BuildImageRes;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理构建镜像的请求连接，该类的实例只有一个(单例)
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-05 21:10 <br/>
 */
@Component
@Slf4j
public class BuildImageHandler extends AbstractWebSocketHandler {

    // TODO 使用线程池 ThreadPoolTaskExecutor

    /**
     * 所有构建镜像的任务 镜像配置ID -> 构建任务
     */
    private static final ConcurrentHashMap<Long, BuildImageTask> BUILD_IMAGE_TASK_MAP = new ConcurrentHashMap<>();

    static {
        // 守护线程
        Thread thread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                List<Long> rmList = new ArrayList<>();
                for (ConcurrentHashMap.Entry<Long, BuildImageTask> entry : BUILD_IMAGE_TASK_MAP.entrySet()) {
                    if (!entry.getValue().isAlive()) {
                        // TODO 调用 BuildImageTask 释放资源的方法
                        rmList.add(entry.getKey());
                    }
                }
                for (Long key : rmList) {
                    BUILD_IMAGE_TASK_MAP.remove(key);
                }
                log.info("[BuildImageHandler] 移除务数[{}] 当前正在构建任务数[{}]", rmList.size(), BUILD_IMAGE_TASK_MAP.size());
                try {
                    Thread.sleep(1000 * 3);
                } catch (Throwable e) {
                    log.error("休眠失败", e);
                }
            }
        });
        thread.start();
    }

    @Autowired
    private GlobalConfig globalConfig;
    @Autowired
    private CodeRepositoryMapper codeRepositoryMapper;
    @Autowired
    private ImageConfigMapper imageConfigMapper;

    /**
     * 建立连接后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[BuildImageHandler] 建立连接");
    }

    /**
     * 消息处理，在客户端通过 WebSocket API 发送的消息会经过这里，然后进行相应的处理
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 判断当前session是否已经有一个在构建的镜像
        for (BuildImageTask buildImageTask : BUILD_IMAGE_TASK_MAP.values()) {
            if (buildImageTask.contains(session)) {
                buildImageTask.removeWebSocketSession(session.getId());
            }
        }
        log.info("[BuildImageHandler] 消息处理 -> {}", message.getPayload());
        BuildImageReq buildImageReq = JacksonMapper.nonEmptyMapper().fromJson(message.getPayload(), BuildImageReq.class);
        // 校验请求消息
        if (buildImageReq == null) {
            sendErrorMessage(session, "请求消息格式错误");
            return;
        }
        // 校验参数 BuildImageReq 的完整性
        try {
            BaseValidatorUtils.validateThrowException(ValidatorFactoryUtils.getHibernateValidator(), buildImageReq);
        } catch (ConstraintViolationException e) {
            log.info("请求参数校验失败", e);
            sendErrorMessage(session, JacksonMapper.nonEmptyMapper().toJson(BaseValidatorUtils.extractMessage(e)));
        }
        // 业务校验 - 校验对应的配置信息都存在
        ImageConfig imageConfig = imageConfigMapper.selectByPrimaryKey(buildImageReq.getImageConfigId());
        if (imageConfig == null) {
            sendErrorMessage(session, String.format("Docker镜像配置不存在，ImageConfigId=%1$s", buildImageReq.getImageConfigId()));
            return;
        }
        CodeRepository codeRepository = codeRepositoryMapper.selectByPrimaryKey(imageConfig.getRepositoryId());
        if (codeRepository == null) {
            sendErrorMessage(session, String.format("代码仓库不存在，ImageConfigId=%1$s", imageConfig.getRepositoryId()));
            return;
        }
        // 当前镜像构建状态(0：未构建, 1：正在下载代码, 2：正在编译代码, 3：正在构建镜像, S：构建成功, F：构建失败)
        if (Objects.equals(ImageConfig.buildState_1, imageConfig.getBuildState())
                || Objects.equals(ImageConfig.buildState_2, imageConfig.getBuildState())
                || Objects.equals(ImageConfig.buildState_3, imageConfig.getBuildState())) {
            BuildImageTask buildImageTask = BUILD_IMAGE_TASK_MAP.get(buildImageReq.getImageConfigId());
            if (buildImageTask == null) {
                sendErrorMessage(session, String.format("当前镜像正在构建，ImageConfigId=%1$s", imageConfig.getRepositoryId()));
                WebSocketCloseSessionUtils.closeSession(session);
            } else {
                buildImageTask.addWebSocketSession(session);
            }
            return;
        }
        // 启动任务前校验 - 当前构建镜像任务数是否已达到最大值
        if (BUILD_IMAGE_TASK_MAP.size() >= globalConfig.getMaxBuildImageTask()) {
            sendErrorMessage(session, String.format("当前构建镜像任务数已达到最大值:%1$s，请稍候再试", globalConfig.getMaxBuildImageTask()));
        }
        // 启动任务
        BuildImageTask buildImageTask = BuildImageTask.newBuildImageTask(session, codeRepository, imageConfig);
        buildImageTask.start();
        BUILD_IMAGE_TASK_MAP.put(buildImageReq.getImageConfigId(), buildImageTask);
    }

    /**
     * 消息传输错误处理
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("消息传输错误处理", exception);
    }

    /**
     * 关闭连接后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("关闭连接后");
        WebSocketCloseSessionUtils.closeSession(session);
    }

    /**
     * 支持部分消息
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 1.发送一个错误消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param errorMessage 错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        BuildImageRes buildImageRes = new BuildImageRes();
        buildImageRes.setLogText(errorMessage);
        buildImageRes.setComplete(true);
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(buildImageRes));
        try {
            session.sendMessage(textMessage);
        } catch (Throwable e) {
            throw ExceptionUtils.unchecked(e);
        }
        // 关闭连接
        WebSocketCloseSessionUtils.closeSession(session);
    }
}
