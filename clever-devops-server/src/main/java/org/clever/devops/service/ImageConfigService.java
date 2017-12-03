package org.clever.devops.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.server.service.BaseService;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.devops.dto.request.ImageConfigAddDto;
import org.clever.devops.dto.request.ImageConfigQueryDto;
import org.clever.devops.dto.request.ImageConfigUpdateDto;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.clever.devops.utils.GitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * 作者： lzw<br/>
 * 创建时间：2017-12-03 8:56 <br/>
 */
@Service
@Slf4j
public class ImageConfigService extends BaseService {

    @Autowired
    private CodeRepositoryMapper codeRepositoryMapper;

    @Autowired
    private ImageConfigMapper imageConfigMapper;

    /**
     * 新增Docker镜像配置
     */
    @Transactional
    public ImageConfig addImageConfig(ImageConfigAddDto imageConfigAddDto) {
        // 获取代码仓库信息
        CodeRepository codeRepository = codeRepositoryMapper.selectByPrimaryKey(imageConfigAddDto.getRepositoryId());
        if (codeRepository == null) {
            throw new BusinessException(String.format("代码仓库不存在，RepositoryId=%1$s", imageConfigAddDto.getRepositoryId()));
        }
        // 校验代码仓库“branch或Tag”是否存在
        ImageConfig.GitBranch gitBranch = branchIsExists(
                codeRepository.getRepositoryUrl(),
                codeRepository.getAuthorizationType().toString(),
                codeRepository.getAuthorizationInfo(),
                imageConfigAddDto.getBranch());
        if (gitBranch == null) {
            throw new BusinessException(String.format("“branch或Tag”不存在，branch=%1$s", imageConfigAddDto.getBranch()));
        }
        // 校验镜像配置已经存在
        ImageConfig tmp = imageConfigMapper.getByRepositoryId(imageConfigAddDto.getRepositoryId(), gitBranch.getCommitId());
        if (tmp != null) {
            throw new BusinessException(String.format("Docker镜像配置已经存在，RepositoryId=%1$s, CommitId=%2$s", imageConfigAddDto.getRepositoryId(), gitBranch.getCommitId()));
        }
        // 校验 serverUrl 唯一
        tmp = imageConfigMapper.getByServerUrl(imageConfigAddDto.getServerUrl());
        if (tmp != null) {
            throw new BusinessException(String.format("服务访问域名重复，ServerUrl=%1$s", imageConfigAddDto.getServerUrl()));
        }
        // 保存数据
        ImageConfig imageConfig = BeanMapper.mapper(imageConfigAddDto, ImageConfig.class);
        imageConfig.setCommitId(gitBranch.getCommitId());
        imageConfig.setCreateBy("");
        imageConfig.setCreateDate(new Date());
        imageConfigMapper.insertSelective(imageConfig);
        return imageConfig;
    }

    /**
     * 查询代码仓库
     */
    public PageInfo<ImageConfig> findImageConfig(ImageConfigQueryDto imageConfigQueryDto) {
        return PageHelper
                .startPage(imageConfigQueryDto.getPageNo(), imageConfigQueryDto.getPageSize())
                .doSelectPageInfo(() -> imageConfigMapper.findImageConfig(imageConfigQueryDto));
    }

    /**
     * 获取Docker镜像配置
     */
    public ImageConfig getImageConfig(String serverUrl) {
        return imageConfigMapper.getByServerUrl(serverUrl);
    }

    /**
     * 更新Docker镜像配置
     */
    @Transactional
    public ImageConfig updateImageConfig(Long id, ImageConfigUpdateDto imageConfigUpdateDto) {
        // 查询需要更新的数据
        ImageConfig imageConfig = imageConfigMapper.selectByPrimaryKey(id);
        if (imageConfig == null) {
            throw new BusinessException(String.format("Docker镜像配置不存在，ID=%1$s", id));
        }
        // 跟新了 Branch ，需要校验
        if (imageConfigUpdateDto.getBranch() != null && !Objects.equals(imageConfig.getBranch(), imageConfigUpdateDto.getBranch())) {
            // 获取代码仓库信息
            CodeRepository codeRepository = codeRepositoryMapper.selectByPrimaryKey(imageConfig.getRepositoryId());
            if (codeRepository == null) {
                throw new BusinessException(String.format("代码仓库不存在，RepositoryId=%1$s", imageConfig.getRepositoryId()));
            }
            // 校验代码仓库“branch或Tag”是否存在
            ImageConfig.GitBranch gitBranch = branchIsExists(
                    codeRepository.getRepositoryUrl(),
                    codeRepository.getAuthorizationType().toString(),
                    codeRepository.getAuthorizationInfo(),
                    imageConfigUpdateDto.getBranch());
            if (gitBranch == null) {
                throw new BusinessException(String.format("“branch或Tag”不存在，branch=%1$s", imageConfigUpdateDto.getBranch()));
            }
            // 校验镜像配置已经存在
            ImageConfig tmp = imageConfigMapper.getByRepositoryId(imageConfig.getRepositoryId(), imageConfig.getCommitId());
            if (tmp != null && !Objects.equals(imageConfig.getId(), tmp.getId())) {
                throw new BusinessException(String.format("Docker镜像配置已经存在，RepositoryId=%1$s, CommitId=%2$s", imageConfig.getRepositoryId(), imageConfig.getCommitId()));
            }
            // 更新 CommitId
            imageConfig.setCommitId(gitBranch.getCommitId());
        }
        // 更新了 serverUrl，需要校验
        if (imageConfigUpdateDto.getServerUrl() != null) {
            // 校验 serverUrl 唯一
            ImageConfig tmp = imageConfigMapper.getByServerUrl(imageConfigUpdateDto.getServerUrl());
            if (tmp != null && !Objects.equals(imageConfig.getId(), tmp.getId())) {
                throw new BusinessException(String.format("服务访问域名重复，ServerUrl=%1$s", imageConfigUpdateDto.getServerUrl()));
            }
        }
        // 更新数据
        BeanMapper.copyTo(imageConfigUpdateDto, imageConfig);
        imageConfig.setUpdateBy("");
        imageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(imageConfig);
        imageConfig = imageConfigMapper.selectByPrimaryKey(id);
        return imageConfig;
    }

    /**
     * 获取“branch或Tag”信息<br/>
     *
     * @param repositoryUrl     代码仓库地址
     * @param authorizationType 代码仓库授权类型(0：不需要授权；1：用户名密码；)
     * @param authorizationInfo 代码仓库授权信息
     * @param branch            branch或Tag
     */
    private ImageConfig.GitBranch branchIsExists(String repositoryUrl, String authorizationType, String authorizationInfo, String branch) {
        if (Objects.equals(CodeRepository.Authorization_Type_0.toString(), authorizationType)) {
            // 没有访问限制
            return GitUtils.getBranch(repositoryUrl, branch);
        } else if (Objects.equals(CodeRepository.Authorization_Type_1.toString(), authorizationType)) {
            // 需要用户名、密码访问
            CodeRepository.UserNameAndPassword userNameAndPassword = JacksonMapper.nonEmptyMapper().fromJson(authorizationInfo, CodeRepository.UserNameAndPassword.class);
            if (userNameAndPassword == null) {
                throw new BusinessException("读取授权用户名密码失败");
            }
            return GitUtils.getBranch(repositoryUrl, userNameAndPassword.getUsername(), userNameAndPassword.getPassword(), branch);
        } else {
            throw new BusinessException("不支持的代码仓库授权类型");
        }
    }
}
