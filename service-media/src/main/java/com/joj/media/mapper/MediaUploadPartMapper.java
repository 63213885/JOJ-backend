package com.joj.media.mapper;

import com.joj.common.core.model.entity.MediaUploadPart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:57
 */

@Mapper
public interface MediaUploadPartMapper {

    int insertOrUpdatePart(MediaUploadPart part);

    List<MediaUploadPart> listByTaskId(@Param("taskId") Long taskId);

    List<Integer> listUploadedPartNumbers(@Param("taskId") Long taskId);
}
