package org.clever.devops.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.response.BaseResponse;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;

/**
 * 构建镜像的响应结果
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-04 14:22 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BuildImageResDto extends BaseResponse {

    /**
     * 开始构建时的时间戳
     */
    private Long startTime;

    /**
     * 当前操作的“代码仓库”
     */
    private CodeRepository codeRepository;

    /**
     * 当前操作的“Docker镜像配置”
     */
    private ImageConfig imageConfig;

    /**
     * 当前镜像构建状态(0：未构建, 1：正在下载代码, 2：正在编译代码, 3：正在构建镜像, S：构建成功, F：构建失败)
     */
    private Integer buildState;

    /**
     * 日志信息
     */
    private String logText;

    /**
     * 完成时的消息
     */
    private String completeMsg;
}
