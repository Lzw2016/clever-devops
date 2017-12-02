package org.clever.devops.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.dto.request.CodeRepositoryAddDto;
import org.clever.devops.dto.request.CodeRepositoryQueryDto;
import org.clever.devops.dto.request.CodeRepositoryUpdateDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.utils.GitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:23 <br/>
 */
@Service
public class CodeRepositoryService {

    @Autowired
    private CodeRepositoryMapper codeRepositoryMapper;

    /**
     * 新增代码仓库
     */
    @Transactional
    public CodeRepository addCodeRepository(CodeRepositoryAddDto codeRepositoryAddDto) {
        // 校验项目名称是否已经存在
        CodeRepository codeRepository = codeRepositoryMapper.getByProjectName(codeRepositoryAddDto.getProjectName());
        if (codeRepository != null) {
            throw new BusinessException(String.format("项目名称已经存在，ProjectName=%1$s", codeRepositoryAddDto.getProjectName()));
        }
        // 校验代码仓库类型
        if (!Objects.equals(codeRepositoryAddDto.getRepositoryType(), CodeRepository.Repository_Type_Git)) {
            throw new BusinessException("当前只支持GIT仓库");
        }
        // 测试连接代码仓库地址
        testConnect(codeRepositoryAddDto.getRepositoryUrl(), codeRepositoryAddDto.getAuthorizationType(), codeRepositoryAddDto.getAuthorizationInfo());
        // 保存数据
        codeRepository = BeanMapper.mapper(codeRepositoryAddDto, CodeRepository.class);
        codeRepository.setCreateBy("");
        codeRepository.setCreateDate(new Date());
        codeRepositoryMapper.insertSelective(codeRepository);
        return codeRepository;
    }

    /**
     * 查询代码仓库
     */
    public PageInfo<CodeRepository> findCodeRepository(CodeRepositoryQueryDto codeRepositoryQueryDto) {
        return PageHelper
                .startPage(codeRepositoryQueryDto.getPageNo(), codeRepositoryQueryDto.getPageSize())
                .doSelectPageInfo(() -> codeRepositoryMapper.findCodeRepository(codeRepositoryQueryDto));
    }

    /**
     * 获取代码仓库
     */
    public CodeRepository getCodeRepository(String projectName) {
        return codeRepositoryMapper.getByProjectName(projectName);
    }

    /**
     * 更新代码仓库
     *
     * @param id                      代码仓库ID
     * @param codeRepositoryUpdateDto 代码仓库更新数据
     */
    @Transactional
    public CodeRepository updateCodeRepository(Long id, CodeRepositoryUpdateDto codeRepositoryUpdateDto) {
        CodeRepository codeRepository = codeRepositoryMapper.selectByPrimaryKey(id);
        if (codeRepository == null) {
            throw new BusinessException(String.format("代码仓库不存在，ID=%1$s", id));
        }
        // 验证 项目名称 是否重复
        if (codeRepositoryUpdateDto.getProjectName() != null) {
            CodeRepository tmp = codeRepositoryMapper.getByProjectName(codeRepositoryUpdateDto.getProjectName());
            if (tmp != null && !Objects.equals(codeRepository.getId(), tmp.getId())) {
                throw new BusinessException(String.format("项目名称已经存在，ProjectName=%1$s", codeRepositoryUpdateDto.getProjectName()));
            }
        }
        // 校验代码仓库类型
        if (codeRepositoryUpdateDto.getRepositoryType() != null
                && !Objects.equals(codeRepositoryUpdateDto.getRepositoryType(), CodeRepository.Repository_Type_Git)) {
            throw new BusinessException("当前只支持GIT仓库");
        }
        // 测试连接代码仓库地址
        if (codeRepositoryUpdateDto.getRepositoryUrl() != null
                || codeRepositoryUpdateDto.getAuthorizationType() != null
                || codeRepositoryUpdateDto.getAuthorizationInfo() != null) {
            String repositoryUrl = codeRepositoryUpdateDto.getRepositoryUrl() != null ? codeRepositoryUpdateDto.getRepositoryUrl() : codeRepository.getRepositoryUrl();
            String authorizationType = codeRepositoryUpdateDto.getAuthorizationType() != null ? codeRepositoryUpdateDto.getAuthorizationType() : String.valueOf(codeRepository.getAuthorizationType());
            String authorizationInfo = codeRepositoryUpdateDto.getAuthorizationInfo() != null ? codeRepositoryUpdateDto.getAuthorizationInfo() : codeRepository.getAuthorizationInfo();
            testConnect(repositoryUrl, authorizationType, authorizationInfo);
        }
        // 更新数据
        BeanMapper.copyTo(codeRepositoryUpdateDto, codeRepository);
        codeRepository.setUpdateBy("");
        codeRepository.setUpdateDate(new Date());
        codeRepositoryMapper.updateByPrimaryKeySelective(codeRepository);
        codeRepository = codeRepositoryMapper.selectByPrimaryKey(id);
        return codeRepository;
    }

    /**
     * 删除代码仓库
     */
    @Transactional
    public CodeRepository delete(String projectName) {
        CodeRepository codeRepository = codeRepositoryMapper.getByProjectName(projectName);
        if (codeRepository == null) {
            throw new BusinessException(String.format("项目名称不存在，ProjectName=%1$s", projectName));
        }
        // 校验当前代码仓库是否被依赖

        // TODO 删除代码仓库
        return codeRepository;
    }

    /**
     * 测试连接代码仓库地址 (失败抛出异常)
     *
     * @param repositoryUrl     代码仓库地址
     * @param authorizationType 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     * @param authorizationInfo 代码仓库授权信息
     */
    private void testConnect(String repositoryUrl, String authorizationType, String authorizationInfo) {
        if (Objects.equals(CodeRepository.Authorization_Type_0.toString(), authorizationType)) {
            // 没有访问限制
            GitUtils.testConnect(repositoryUrl);
        } else if (Objects.equals(CodeRepository.Authorization_Type_1.toString(), authorizationType)) {
            // 需要用户名、密码访问
            CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(authorizationInfo, CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
            GitUtils.testConnect(repositoryUrl, userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
        } else {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
    }
}
