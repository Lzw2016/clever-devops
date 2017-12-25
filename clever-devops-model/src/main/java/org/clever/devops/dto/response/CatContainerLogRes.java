package org.clever.devops.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.response.BaseResponse;

/**
 * 查看Docker容器日志响应
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-24 19:20 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CatContainerLogRes extends BaseResponse {

    /**
     * 日志信息
     */
    private String logText;

    /**
     * 处理完成要求客户端主动关闭连接
     */
    private boolean complete = false;

    public CatContainerLogRes() {
    }

    public CatContainerLogRes(String logText, boolean complete) {
        this.logText = logText;
        this.complete = complete;
    }
}
