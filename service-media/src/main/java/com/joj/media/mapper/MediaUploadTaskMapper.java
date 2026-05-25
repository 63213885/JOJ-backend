package com.joj.media.mapper;

import com.joj.common.core.model.entity.MediaUploadTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:57
 */

@Mapper
public interface MediaUploadTaskMapper {

    int insertUploadTask(MediaUploadTask task);

    MediaUploadTask selectById(@Param("id") Long id);

    MediaUploadTask selectUploadingTask(@Param("bucketName") String bucketName,
                                        @Param("md5") String md5,
                                        @Param("fileSize") Long fileSize);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status);

    int updateStatusAndMediaFileId(@Param("id") Long id,
                                   @Param("status") Integer status,
                                   @Param("mediaFileId") Long mediaFileId);
}
