package org.clever.devops.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.server.service.BaseService;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.devops.dto.request.CodeRepositoryAddReq;
import org.clever.devops.dto.request.CodeRepositoryQueryReq;
import org.clever.devops.dto.request.CodeRepositoryUpdateReq;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.clever.devops.utils.CodeRepositoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-02 15:23 <br/>
 */
@Service
public class CodeRepositoryService extends BaseService {

    @Autowired
    private CodeRepositoryMapper codeRepositoryMapper;
    @Autowired
    private ImageConfigMapper imageConfigMapper;

    /**
     * 新增代码仓库
     */
    @Transactional
    public CodeRepository addCodeRepository(CodeRepositoryAddReq codeRepositoryAddReq) {
        // 校验项目名称是否已经存在
        CodeRepository codeRepository = codeRepositoryMapper.getByProjectName(codeRepositoryAddReq.getProjectName());
        if (codeRepository != null) {
            throw new BusinessException(String.format("项目名称已经存在，ProjectName=%1$s", codeRepositoryAddReq.getProjectName()));
        }
        // 校验代码仓库类型
        if (!Objects.equals(codeRepositoryAddReq.getRepositoryType(), CodeRepository.Repository_Type_Git)) {
            throw new BusinessException("当前只支持GIT仓库");
        }
        // 构造 CodeRepository
        codeRepository = BeanMapper.mapper(codeRepositoryAddReq, CodeRepository.class);
        codeRepository.setCreateBy("");
        codeRepository.setCreateDate(new Date());
        // 测试连接代码仓库地址
        CodeRepositoryUtils.testConnect(codeRepository);
        // 保存数据
        codeRepositoryMapper.insertSelective(codeRepository);
        return codeRepository;
    }

    /**
     * 查询代码仓库
     */
    public PageInfo<CodeRepository> findCodeRepository(CodeRepositoryQueryReq codeRepositoryQueryReq) {
        return PageHelper
                .startPage(codeRepositoryQueryReq.getPageNo(), codeRepositoryQueryReq.getPageSize())
                .doSelectPageInfo(() -> codeRepositoryMapper.findCodeRepository(codeRepositoryQueryReq));
    }

    /**
     * 获取代码仓库
     */
    public CodeRepository getCodeRepository(String projectName) {
        return codeRepositoryMapper.getByProjectName(projectName);
    }

    /**
     * 获取代码仓库
     */
    public CodeRepository getCodeRepository(Long id) {
        return codeRepositoryMapper.selectByPrimaryKey(id);
    }

    /**
     * 更新代码仓库
     *
     * @param id                      代码仓库ID
     * @param codeRepositoryUpdateReq 代码仓库更新数据
     */
    @Transactional
    public CodeRepository updateCodeRepository(Long id, CodeRepositoryUpdateReq codeRepositoryUpdateReq) {
        CodeRepository codeRepository = codeRepositoryMapper.selectByPrimaryKey(id);
        if (codeRepository == null) {
            throw new BusinessException(String.format("代码仓库不存在，ID=%1$s", id));
        }
        // 校验其对应的所有对应是否在构建中
        int buildingCount = imageConfigMapper.getBuildingCount(codeRepository.getId());
        if (buildingCount > 0) {
            throw new BusinessException("当前代码仓库下存在Docker镜像正在构建中，不能修改");
        }
        // 验证 项目名称 是否重复
        if (codeRepositoryUpdateReq.getProjectName() != null) {
            CodeRepository tmp = codeRepositoryMapper.getByProjectName(codeRepositoryUpdateReq.getProjectName());
            if (tmp != null && !Objects.equals(codeRepository.getId(), tmp.getId())) {
                throw new BusinessException(String.format("项目名称已经存在，ProjectName=%1$s", codeRepositoryUpdateReq.getProjectName()));
            }
        }
        // 校验代码仓库类型
        if (codeRepositoryUpdateReq.getRepositoryType() != null
                && !Objects.equals(codeRepositoryUpdateReq.getRepositoryType(), CodeRepository.Repository_Type_Git)) {
            throw new BusinessException("当前只支持GIT仓库");
        }
        // 测试连接代码仓库地址
        if (codeRepositoryUpdateReq.getRepositoryUrl() != null
                || codeRepositoryUpdateReq.getAuthorizationType() != null
                || codeRepositoryUpdateReq.getAuthorizationInfo() != null) {
            String repositoryUrl = codeRepositoryUpdateReq.getRepositoryUrl() != null ? codeRepositoryUpdateReq.getRepositoryUrl() : codeRepository.getRepositoryUrl();
            String authorizationType = codeRepositoryUpdateReq.getAuthorizationType() != null ? codeRepositoryUpdateReq.getAuthorizationType() : String.valueOf(codeRepository.getAuthorizationType());
            String authorizationInfo = codeRepositoryUpdateReq.getAuthorizationInfo() != null ? codeRepositoryUpdateReq.getAuthorizationInfo() : codeRepository.getAuthorizationInfo();
            CodeRepositoryUtils.testConnect(repositoryUrl, authorizationType, authorizationInfo);
        }
        // 更新数据
        BeanMapper.copyTo(codeRepositoryUpdateReq, codeRepository);
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
        List<ImageConfig> list = imageConfigMapper.getByRepositoryId(codeRepository.getId());
        if (list.size() > 0) {
            throw new BusinessException(String.format("不能删除，存在%1$s个服务配置依赖当前代码仓库", list.size()));
        }
        // 删除代码仓库
        codeRepositoryMapper.deleteByPrimaryKey(codeRepository.getId());
        return codeRepository;
    }
}
