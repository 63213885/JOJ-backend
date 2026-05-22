package com.joj.media.service;

import com.joj.media.model.entity.MediaFile;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 22:52
 */

public interface MediaFileService {

    /**
     * 新增文件记录
     */
    MediaFile addMediaFile(MediaFile mediaFile);

    /**
     * 删除文件记录，逻辑删除
     */
    boolean deleteMediaFile(Long id);

    /**
     * 判断同 bucket 下是否已存在相同文件
     */
    boolean exists(String bucketName, String md5, Long fileSize);

    /**
     * 查询同 bucket 下的相同文件
     */
    MediaFile getSameFile(String bucketName, String md5, Long fileSize);

    MediaFile getById(Long id);

    MediaFile getByBucketObject(String bucketName, String objectName);

}
