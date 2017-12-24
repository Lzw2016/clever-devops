package org.clever.devops.service;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.clever.common.model.exception.BusinessException;
import org.clever.common.server.service.BaseService;
import org.clever.common.utils.mapper.BeanMapper;
import org.clever.devops.dto.request.ImageConfigAddReq;
import org.clever.devops.dto.request.ImageConfigQueryReq;
import org.clever.devops.dto.request.ImageConfigUpdateReq;
import org.clever.devops.entity.CodeRepository;
import org.clever.devops.entity.ImageConfig;
import org.clever.devops.mapper.CodeRepositoryMapper;
import org.clever.devops.mapper.ImageConfigMapper;
import org.clever.devops.utils.CodeRepositoryUtils;
import org.clever.devops.utils.ImageConfigUtils;
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
    public ImageConfig addImageConfig(ImageConfigAddReq imageConfigAddReq) {
        // 获取代码仓库信息
        CodeRepository codeRepository = codeRepositoryMapper.selectByPrimaryKey(imageConfigAddReq.getRepositoryId());
        if (codeRepository == null) {
            throw new BusinessException(String.format("代码仓库不存在，RepositoryId=%1$s", imageConfigAddReq.getRepositoryId()));
        }
        // 校验代码仓库“branch或Tag”是否存在
        ImageConfig.GitBranch gitBranch = CodeRepositoryUtils.getBranch(codeRepository, imageConfigAddReq.getBranch());
        if (gitBranch == null) {
            throw new BusinessException(String.format("“branch或Tag”不存在，branch=%1$s", imageConfigAddReq.getBranch()));
        }
        // 校验镜像配置已经存在
        ImageConfig tmp = imageConfigMapper.getByRepositoryId(imageConfigAddReq.getRepositoryId(), gitBranch.getCommitId());
        if (tmp != null) {
            throw new BusinessException(String.format("Docker镜像配置已经存在，RepositoryId=%1$s, CommitId=%2$s", imageConfigAddReq.getRepositoryId(), gitBranch.getCommitId()));
        }
        // 校验 serverUrl 唯一
        tmp = imageConfigMapper.getByServerUrl(imageConfigAddReq.getServerUrl());
        if (tmp != null) {
            throw new BusinessException(String.format("服务访问域名重复，ServerUrl=%1$s", imageConfigAddReq.getServerUrl()));
        }
        // 保存数据
        ImageConfig imageConfig = BeanMapper.mapper(imageConfigAddReq, ImageConfig.class);
        imageConfig.setCommitId(gitBranch.getCommitId());
        imageConfig.setCreateBy("");
        imageConfig.setCreateDate(new Date());
        imageConfigMapper.insertSelective(imageConfig);
        return imageConfig;
    }

    /**
     * 查询代码仓库
     */
    public PageInfo<ImageConfig> findImageConfig(ImageConfigQueryReq imageConfigQueryReq) {
        return PageHelper
                .startPage(imageConfigQueryReq.getPageNo(), imageConfigQueryReq.getPageSize())
                .doSelectPageInfo(() -> imageConfigMapper.findImageConfig(imageConfigQueryReq));
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
    public ImageConfig updateImageConfig(Long id, ImageConfigUpdateReq imageConfigUpdateReq) {
        // 查询需要更新的数据
        ImageConfig imageConfig = imageConfigMapper.selectByPrimaryKey(id);
        if (imageConfig == null) {
            throw new BusinessException(String.format("Docker镜像配置不存在，ID=%1$s", id));
        }
        // 校验当前 ImageConfig 是否正在构建 -- 当前镜像构建状态(0：未构建, 1：正在下载代码, 2：正在编译代码, 3：正在构建镜像, S：构建成功, F：构建失败)
        if (ImageConfig.buildState_1.equals(imageConfig.getBuildState())
                || ImageConfig.buildState_2.equals(imageConfig.getBuildState())
                || ImageConfig.buildState_3.equals(imageConfig.getBuildState())) {
            throw new BusinessException("当前Docker镜像正在构建中，不能修改");
        }
        // 更新了 Branch ，需要校验
        if (imageConfigUpdateReq.getBranch() != null && !Objects.equals(imageConfig.getBranch(), imageConfigUpdateReq.getBranch())) {
            // 获取代码仓库信息
            CodeRepository codeRepository = codeRepositoryMapper.selectByPrimaryKey(imageConfig.getRepositoryId());
            if (codeRepository == null) {
                throw new BusinessException(String.format("代码仓库不存在，RepositoryId=%1$s", imageConfig.getRepositoryId()));
            }
            // 校验代码仓库“branch或Tag”是否存在
            ImageConfig.GitBranch gitBranch = CodeRepositoryUtils.getBranch(codeRepository, imageConfigUpdateReq.getBranch());
            if (gitBranch == null) {
                throw new BusinessException(String.format("“branch或Tag”不存在，branch=%1$s", imageConfigUpdateReq.getBranch()));
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
        if (imageConfigUpdateReq.getServerUrl() != null) {
            // 校验 serverUrl 唯一
            ImageConfig tmp = imageConfigMapper.getByServerUrl(imageConfigUpdateReq.getServerUrl());
            if (tmp != null && !Objects.equals(imageConfig.getId(), tmp.getId())) {
                throw new BusinessException(String.format("服务访问域名重复，ServerUrl=%1$s", imageConfigUpdateReq.getServerUrl()));
            }
        }
        // 更新数据
        BeanMapper.copyTo(imageConfigUpdateReq, imageConfig);
        imageConfig.setUpdateBy("");
        imageConfig.setUpdateDate(new Date());
        imageConfigMapper.updateByPrimaryKeySelective(imageConfig);
        imageConfig = imageConfigMapper.selectByPrimaryKey(id);
        return imageConfig;
    }

    /**
     * 删除Docker镜像配置
     */
    public ImageConfig delete(Long id) {
        ImageConfig imageConfig = imageConfigMapper.selectByPrimaryKey(id);
        if (imageConfig == null) {
            throw new BusinessException(String.format("Docker镜像配置不存在，ID=%1$s", id));
        }
        // TODO 校验当前Docker镜像配置是否被依赖

        // TODO 删除Docker镜像配置
        return imageConfig;
    }

    /**
     * 根据ImageConfig生成的镜像新增Docker容器
     */
    public CreateContainerResponse createContainer(Long id) {
        ImageConfig imageConfig = imageConfigMapper.selectByPrimaryKey(id);
        if (imageConfig == null) {
            throw new BusinessException(String.format("Docker镜像配置不存在，ID=%1$s", id));
        }
        if (StringUtils.isBlank(imageConfig.getImageId())) {
            throw new BusinessException("Docker镜像配置从未构建成功过，请先构建Docker镜像");
        }
        return ImageConfigUtils.createContainer(imageConfig);
    }
}
