package org.clever.devops.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.response.BaseResponse;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-09 9:35 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContainerStatsRes extends BaseResponse {

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 监控数据
     */
    private Object stats;

    /**
     * 处理完成要求客户端主动关闭连接
     */
    private boolean complete = false;

    public ContainerStatsRes() {
    }

    public ContainerStatsRes(String errorMsg, Object stats, boolean complete) {
        this.errorMsg = errorMsg;
        this.stats = stats;
        this.complete = complete;
    }
}
