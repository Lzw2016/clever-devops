package org.clever.devops.service;

import org.clever.common.model.exception.BusinessException;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.dto.request.CodeRepositoryAddDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.utils.GitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if (Objects.equals(CodeRepository.Authorization_Type_0.toString(), codeRepositoryAddDto.getAuthorizationType())) {
            // 没有访问限制
            GitUtils.testConnect(codeRepositoryAddDto.getRepositoryUrl());
        } else if (Objects.equals(CodeRepository.Authorization_Type_1.toString(), codeRepositoryAddDto.getAuthorizationType())) {
            // 需要用户名、密码访问
            CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(codeRepositoryAddDto.getAuthorizationInfo(), CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
            GitUtils.testConnect(codeRepositoryAddDto.getRepositoryUrl(), userNameAndPassword.getUsername(), userNameAndPassword.getPassword());
        } else {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
        // 保存数据
        codeRepository = BeanMapper.mapper(codeRepositoryAddDto, CodeRepository.class);
        codeRepository.setCreateBy("");
        codeRepository.setCreateDate(new Date());
        codeRepositoryMapper.insertSelective(codeRepository);
        return codeRepository;
    }
}
