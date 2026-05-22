package com.joj.media.mapper;

import com.joj.media.model.entity.MediaFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 22:47
 */

@Mapper
public interface MediaFileMapper {

    int insertMediaFile(MediaFile mediaFile);

    int deleteMediaFileById(@Param("id") Long id);

    MediaFile selectMediaFileById(@Param("id") Long id);

    MediaFile selectByBucketObject(@Param("bucketName") String bucketName,
                                   @Param("objectName") String objectName);

    MediaFile selectByBucketMd5Size(@Param("bucketName") String bucketName,
                                    @Param("md5") String md5,
                                    @Param("fileSize") Long fileSize);

    boolean existsByBucketMd5Size(@Param("bucketName") String bucketName,
                                  @Param("md5") String md5,
                                  @Param("fileSize") Long fileSize);
}
