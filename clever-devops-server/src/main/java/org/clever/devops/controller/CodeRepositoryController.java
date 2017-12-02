package org.clever.devops.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.dto.request.CodeRepositoryAddDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.service.CodeRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:21 <br/>
 */
@Api(description = "代码仓库")
@RequestMapping("/devops")
@RestController
public class CodeRepositoryController extends BaseController {

    @Autowired
    private CodeRepositoryService codeRepositoryService;

    @ApiOperation("新增代码仓库")
    @PostMapping("/code_repository" + JSON_SUFFIX)
    public CodeRepository addCodeRepository(@RequestBody @Validated CodeRepositoryAddDto codeRepositoryAddDto) {
        return codeRepositoryService.addCodeRepository(codeRepositoryAddDto);
    }
}
