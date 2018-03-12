package org.clever.devops.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.clever.common.model.request.BaseRequest;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-03-12 13:45 <br/>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContainerRemoveReq extends BaseRequest {

    private Boolean forceKill;

    private Boolean removeVolumes;
}
