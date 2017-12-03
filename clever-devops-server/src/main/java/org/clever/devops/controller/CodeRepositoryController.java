package org.clever.devops.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.devops.dto.request.CodeRepositoryAddDto;
import org.clever.devops.dto.request.CodeRepositoryQueryDto;
import org.clever.devops.dto.request.CodeRepositoryUpdateDto;
import org.clever.devops.dto.request.TestGitConnectDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.service.CodeRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("查询代码仓库")
    @GetMapping("/code_repository" + JSON_SUFFIX)
    public PageInfo<CodeRepository> findCodeRepository(CodeRepositoryQueryDto codeRepositoryQueryDto) {
        return codeRepositoryService.findCodeRepository(codeRepositoryQueryDto);
    }

    @ApiOperation("获取代码仓库")
    @GetMapping("/code_repository/{projectName}" + JSON_SUFFIX)
    public CodeRepository getCodeRepository(@PathVariable("projectName") String projectName) {
        return codeRepositoryService.getCodeRepository(projectName);
    }

    @ApiOperation("更新代码仓库")
    @PutMapping("/code_repository/{id}" + JSON_SUFFIX)
    public CodeRepository updateCodeRepository(@PathVariable("id") Long id, @RequestBody @Validated CodeRepositoryUpdateDto codeRepositoryUpdateDto) {
        return codeRepositoryService.updateCodeRepository(id, codeRepositoryUpdateDto);
    }

    @ApiOperation("删除代码仓库")
    @DeleteMapping("/code_repository/{projectName}" + JSON_SUFFIX)
    public CodeRepository delete(@PathVariable("projectName") String projectName) {
        return codeRepositoryService.delete(projectName);
    }

    @ApiOperation("测试连接Git仓库")
    @PostMapping("/git_connect" + JSON_SUFFIX)
    public void testGitConnect(@RequestBody @Validated TestGitConnectDto testGitConnectDto) {
        codeRepositoryService.testConnect(testGitConnectDto.getRepositoryUrl(), testGitConnectDto.getAuthorizationType(), testGitConnectDto.getAuthorizationInfo());
    }
}
