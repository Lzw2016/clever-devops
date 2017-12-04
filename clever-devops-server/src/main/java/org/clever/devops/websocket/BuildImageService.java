package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.config.GlobalConfig;
import org.clever.devops.dto.request.BuildImageReqDto;
import org.clever.devops.dto.response.BuildImageResDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 构建Docker 镜像服务<br/>
 * 每个连接都会新增一个该类型的实例
 */
@Component
@ServerEndpoint("/build_image")
@Slf4j
public class BuildImageService {

    private static String Start_Image_CMD = "";

    /**
     * 所有连接对象
     */
    private static CopyOnWriteArraySet<BuildImageService> buildImageTask = new CopyOnWriteArraySet<>();

    /**
     * 关闭所有标记“closeFlag=true”的连接
     */
    private static void closeSession() {
        List<BuildImageService> rmList = new ArrayList<>();
        for (BuildImageService service : buildImageTask) {
            if (service.session == null || service.closeFlag) {
                if (service.session != null && service.session.isOpen()) {
                    try {
                        service.session.close();
                    } catch (Throwable e) {
                        log.error("关闭构建连接异常", e);
                        continue;
                    }
                }
                rmList.add(service);
            }
        }
        buildImageTask.removeAll(rmList);
    }

    @Autowired
    private GlobalConfig globalConfig;
    @Autowired
    private CodeRepositoryMapper codeRepositoryMapper;
    @Autowired
    private ImageConfigMapper imageConfigMapper;

    /**
     * 需要关闭当前连接
     */
    private boolean closeFlag = false;

    /**
     * 响应发送数据
     */
    private BuildImageResDto buildImageResDto = new BuildImageResDto();

    /**
     * 当前连接Session
     */
    private Session session;

    /**
     * 当前操作的“代码仓库”
     */
    private CodeRepository codeRepository;

    /**
     * 当前操作的“Docker镜像配置”
     */
    private ImageConfig imageConfig;

    /**
     * 连接成功事件
     */
    @OnOpen
    public void onOpen(Session session) {
        // 加入当前连接到 buildImageTask
        this.session = session;
        buildImageTask.add(this);
        // 校验任务数量是否超过了限制
        if (buildImageTask.size() >= globalConfig.getMaxBuildImageTask()) {
            sendCompleteMessage(String.format("当前构建镜像数据已达到最大值:%1$s，请稍候再试", globalConfig.getMaxBuildImageTask()));
        }
        buildImageResDto.setBuildImageTaskCount(buildImageTask.size());
    }

    /**
     * 收到消息事件
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        BuildImageReqDto buildImageReqDto = JacksonMapper.nonEmptyMapper().fromJson(message, BuildImageReqDto.class);
        if (buildImageReqDto == null) {
            sendCompleteMessage(String.format("当前构建镜像数据已达到最大值:%1$s，请稍候再试", globalConfig.getMaxBuildImageTask()));
            return;
        }
        // 校验参数 BuildImageReqDto 的完整性

        // 业务校验
        imageConfig = imageConfigMapper.selectByPrimaryKey(buildImageReqDto.getImageConfigId());
        if (imageConfig == null) {
            sendCompleteMessage(String.format("Docker镜像配置不存在，ImageConfigId=%1$s", buildImageReqDto.getImageConfigId()));
            return;
        }
        codeRepository = codeRepositoryMapper.selectByPrimaryKey(imageConfig.getRepositoryId());
        if (codeRepository == null) {
            sendCompleteMessage(String.format("代码仓库不存在，ImageConfigId=%1$s", imageConfig.getRepositoryId()));
            return;
        }

        // 开始异步构建镜像
        buildImageResDto.setStartTime(System.currentTimeMillis());

        buildImage(buildImageReqDto);
    }

    /**
     * 连接关闭事件
     */
    @OnClose
    public void onClose() {
        closeFlag = true;
        closeSession();
    }

    /**
     * 发生异常时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error(String.format("构建镜像异常 SessionID=[%1$s]", session.getId()), error);
        sendCompleteMessage(String.format("发生错误 -> [%1$s]", error.getMessage()));
    }

    /**
     * 开始构建镜像
     */
    @Async
    protected void buildImage(BuildImageReqDto buildImageReqDto) {
        sendMessage("");
    }

    /**
     * 1.发送一个消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param message 失败的消息
     */
    private void sendCompleteMessage(String message) {
        buildImageResDto.setIsComplete(true);
        buildImageResDto.setCompleteMsg(message);
        try {
            session.getBasicRemote().sendText(JacksonMapper.nonEmptyMapper().toJson(buildImageResDto));
        } catch (Throwable e) {
            throw ExceptionUtils.unchecked(e);
        }
        closeFlag = true;
        // 关闭所有“closeFlag=true”的连接
        closeSession();
    }

    /**
     * 发送消息
     */
    private void sendMessage(String logText) {
        buildImageResDto.setLogText(logText);
        try {
            this.session.getBasicRemote().sendText(JacksonMapper.nonEmptyMapper().toJson(buildImageResDto));
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }
}
