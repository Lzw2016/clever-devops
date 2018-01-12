package org.clever.devops.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.response.BaseResponse;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-12 10:18 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TestConnectRes extends BaseResponse {

    /**
     * 是否连接成功
     */
    private boolean isSuccess;

    public TestConnectRes() {
    }

    public TestConnectRes(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
