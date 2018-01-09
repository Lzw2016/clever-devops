package org.clever.devops.websocket.log;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.exception.ExceptionUtils;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.validator.BaseValidatorUtils;
import org.clever.common.utils.validator.ValidatorFactoryUtils;
import org.clever.devops.dto.request.CatContainerLogReq;
import org.clever.devops.dto.response.CatContainerLogRes;
import org.clever.devops.utils.WebSocketCloseSessionUtils;
import org.clever.devops.websocket.Handler;
import org.clever.devops.websocket.Task;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.ConstraintViolationException;

/**
 * 服务日志查看
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-24 19:01 <br/>
 */
@Component
@Slf4j
public class ContainerLogHandler extends Handler {

    /**
     * 消息处理，在客户端通过 WebSocket API 发送的消息会经过这里，然后进行相应的处理
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 判断当前session是否存在对应的Task
        for (Task task : getAllTask()) {
            if (task.contains(session)) {
                task.removeWebSocketSession(session.getId());
            }
        }
        log.info("[ContainerLogHandler] 消息处理 -> {}", message.getPayload());
        CatContainerLogReq catContainerLogReq = JacksonMapper.nonEmptyMapper().fromJson(message.getPayload(), CatContainerLogReq.class);
        // 校验请求消息
        if (catContainerLogReq == null) {
            sendErrorMessage(session, "请求消息格式错误");
            return;
        }
        // 校验参数 CatContainerLogReq 的完整性
        try {
            BaseValidatorUtils.validateThrowException(ValidatorFactoryUtils.getHibernateValidator(), catContainerLogReq);
        } catch (ConstraintViolationException e) {
            log.info("请求参数校验失败", e);
            sendErrorMessage(session, JacksonMapper.nonEmptyMapper().toJson(BaseValidatorUtils.extractMessage(e)));
        }
        // 新建查看日志任务
        Task task = getTaskByTaskId(ContainerLogTask.getTaskId(catContainerLogReq));
        if (task != null) {
            task.addWebSocketSession(session);
        } else {
            ContainerLogTask containerLogTask = ContainerLogTask.newContainerLogTask(session, catContainerLogReq);
            putAndStartTask(containerLogTask);
        }
    }

    /**
     * 1.发送一个错误消息 <br/>
     * 2.服务端主动关闭连接 <br/>
     *
     * @param errorMessage 错误消息
     */
    @Override
    protected void sendErrorMessage(WebSocketSession session, String errorMessage) {
        CatContainerLogRes catContainerLogRes = new CatContainerLogRes();
        catContainerLogRes.setLogText(errorMessage);
        catContainerLogRes.setComplete(true);
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(catContainerLogRes));
        try {
            session.sendMessage(textMessage);
        } catch (Throwable e) {
            throw ExceptionUtils.unchecked(e);
        }
        // 关闭连接
        WebSocketCloseSessionUtils.closeSession(session);
    }
}
