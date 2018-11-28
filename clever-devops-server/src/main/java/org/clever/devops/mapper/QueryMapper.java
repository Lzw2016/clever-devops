package org.clever.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.clever.devops.dto.request.ImageConfigQueryReq;
import org.clever.devops.dto.response.ImageConfigQueryRes;
import org.clever.devops.entity.CodeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-13 10:16 <br/>
 */
@Repository
@Mapper
public interface QueryMapper extends BaseMapper<CodeRepository> {

    List<ImageConfigQueryRes> findImageConfig(@Param("query") ImageConfigQueryReq imageConfigQueryReq, IPage page);
}
