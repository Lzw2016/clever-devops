package org.clever.devops.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.clever.common.server.controller.BaseController;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.devops.dto.request.*;
import org.clever.devops.dto.response.TestConnectRes;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.service.CodeRepositoryService;
import org.clever.devops.utils.CodeRepositoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:21 <br/>
 */
@Api(description = "代码仓库")
@RequestMapping("/api/devops")
@RestController
public class CodeRepositoryController extends BaseController {

    @Autowired
    private CodeRepositoryService codeRepositoryService;

    @ApiOperation("新增代码仓库")
    @PostMapping("/code_repository" + JSON_SUFFIX)
    public CodeRepository addCodeRepository(@RequestBody @Validated CodeRepositoryAddReq codeRepositoryAddReq) {
        return codeRepositoryService.addCodeRepository(codeRepositoryAddReq);
    }

    @ApiOperation("查询代码仓库")
    @GetMapping("/code_repository" + JSON_SUFFIX)
    public IPage<CodeRepository> findCodeRepository(CodeRepositoryQueryReq codeRepositoryQueryReq) {
        return codeRepositoryService.findCodeRepository(codeRepositoryQueryReq);
    }

    @ApiOperation("获取代码仓库")
    @GetMapping("/code_repository/{projectName}" + JSON_SUFFIX)
    public CodeRepository getCodeRepository(@PathVariable String projectName) {
        return codeRepositoryService.getCodeRepository(projectName);
    }

    @ApiOperation("获取代码仓库")
    @GetMapping("/code_repository/id/{id}" + JSON_SUFFIX)
    public CodeRepository getCodeRepository(@PathVariable Long id) {
        return codeRepositoryService.getCodeRepository(id);
    }

    @ApiOperation("更新代码仓库")
    @PutMapping("/code_repository/{id}" + JSON_SUFFIX)
    public CodeRepository updateCodeRepository(@PathVariable Long id, @RequestBody @Validated CodeRepositoryUpdateReq codeRepositoryUpdateReq) {
        return codeRepositoryService.updateCodeRepository(id, codeRepositoryUpdateReq);
    }

    @ApiOperation("删除代码仓库")
    @DeleteMapping("/code_repository/{projectName}" + JSON_SUFFIX)
    public CodeRepository delete(@PathVariable String projectName) {
        return codeRepositoryService.delete(projectName);
    }

    @ApiOperation("测试连接Git仓库")
    @PostMapping("/code_repository/test_connect" + JSON_SUFFIX)
    public TestConnectRes testConnect(@RequestBody @Validated TestGitConnectReq testGitConnectReq) {
        CodeRepositoryUtils.testConnect(testGitConnectReq.getRepositoryUrl(), testGitConnectReq.getAuthorizationType(), testGitConnectReq.getAuthorizationInfo());
        return new TestConnectRes(true);
    }

    @ApiOperation("获取所有的“branch或Tag”信息")
    @PostMapping("/code_repository/git_branch" + JSON_SUFFIX)
    public List<ImageConfig.GitBranch> getGitBranch(@RequestBody @Validated GetGitBranchReq getGitBranchReq) {
        return CodeRepositoryUtils.getAllBranch(BeanMapper.mapper(getGitBranchReq, CodeRepository.class));
    }
}
