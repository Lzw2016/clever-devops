package org.clever.devops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.config.GlobalConfig;
import org.clever.devops.dto.request.BuildImageReqDto;
import org.clever.devops.dto.response.BuildImageResDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
     * 当前连接Session
     */
    private Session session;

    /**
     * 需要关闭当前连接
     */
    private boolean closeFlag = false;

    /**
     * 当前操作的“代码仓库”
     */
    private CodeRepository codeRepository;

    /**
     * 当前操作的“Docker镜像配置”
     */
    private ImageConfig imageConfig;

    /**
     * 响应发送数据
     */
    private BuildImageResDto buildImageResDto;


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
            closeFlag = true;
            // TODO 发送提示信息
        }
        // 关闭校验未通过的连接
        closeSession();
    }

    /**
     * 连接关闭事件
     */
    @OnClose
    public void onClose() {
        buildImageTask.remove(this);
        System.out.println("有一链接关闭!当前在线人数为" + buildImageTask.size());
    }

    /**
     * 发生异常时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误 -> {}", error);
    }

    /**
     * 收到消息事件
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("来自客户端的消息:  | [" + buildImageTask.size() + "] | " + message);
        // 群发消息
//        for (BuildImageService item : buildImageTask) {
//            item.sendMessage("[" + buildImageTask.size() + "] | " + message);
//        }
//
//        for (int i = 0; i < 100; i++) {
//            this.sendMessage("服务端消息：i = " + i);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 开始构建镜像
     */
    private void buildImage(BuildImageReqDto buildImageReqDto) {

    }

    /**
     * 发送消息
     */
    private void sendMessage(BuildImageResDto message) throws IOException {
        this.session.getBasicRemote().sendText(JacksonMapper.nonEmptyMapper().toJson(message));
    }
}
