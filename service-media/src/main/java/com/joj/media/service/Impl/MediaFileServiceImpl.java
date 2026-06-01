package com.joj.media.service.Impl;

import com.joj.media.mapper.MediaFileMapper;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.media.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 22:53
 */

@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private final MediaFileMapper mediaFileMapper;

    /**
     * 新增文件记录
     */
    @Transactional(rollbackFor = Exception.class)
    public MediaFile addMediaFile(MediaFile mediaFile) {
        if (mediaFile == null) {
            throw new RuntimeException("文件记录不能为空");
        }
        if (mediaFile.getOriginalFilename() == null || mediaFile.getOriginalFilename().trim().isEmpty()) {
            throw new RuntimeException("原始文件名不能为空");
        }
        if (mediaFile.getMd5() == null || mediaFile.getMd5().trim().isEmpty()) {
            throw new RuntimeException("文件MD5不能为空");
        }
        if (mediaFile.getFileSize() == null || mediaFile.getFileSize() <= 0) {
            throw new RuntimeException("文件大小错误");
        }
        if (mediaFile.getBucketName() == null || mediaFile.getBucketName().trim().isEmpty()) {
            throw new RuntimeException("bucket不能为空");
        }
        if (mediaFile.getObjectName() == null || mediaFile.getObjectName().trim().isEmpty()) {
            throw new RuntimeException("objectName不能为空");
        }
        if (mediaFile.getAccessType() == null) {
            mediaFile.setAccessType(0);
        }

        int insert = mediaFileMapper.insertMediaFile(mediaFile);
        if (insert <= 0) {
            throw new RuntimeException("保存文件记录失败");
        }

        return mediaFile;
    }

    /**
     * 删除文件记录，逻辑删除
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMediaFile(Long id) {
        if (id == null) {
            throw new RuntimeException("文件ID不能为空");
        }

        return mediaFileMapper.deleteMediaFileById(id) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean markTranscoding(Long fileId) {
        if (fileId == null) {
            return false;
        }
        return mediaFileMapper.markTranscoding(fileId) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markTranscodeSuccess(Long fileId, String hlsPrefix, Integer encryptType, byte[] encryptKey, String encryptIv) {
        mediaFileMapper.markTranscodeSuccess(fileId, hlsPrefix, encryptType, encryptKey, encryptIv);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markTranscodeFailed(Long fileId) {
        if (fileId == null) {
            return;
        }
        mediaFileMapper.markTranscodeFailed(fileId);
    }

    /**
     * 判断同 bucket 下是否已存在相同文件
     */
    public boolean exists(String bucketName, String md5, Long fileSize) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new RuntimeException("bucket不能为空");
        }
        if (md5 == null || md5.trim().isEmpty()) {
            throw new RuntimeException("文件MD5不能为空");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new RuntimeException("文件大小错误");
        }

        return mediaFileMapper.existsByBucketMd5Size(bucketName, md5, fileSize);
    }

    /**
     * 查询同 bucket 下的相同文件
     */
    public MediaFile getSameFile(String bucketName, String md5, Long fileSize) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new RuntimeException("bucket不能为空");
        }
        if (md5 == null || md5.trim().isEmpty()) {
            throw new RuntimeException("文件MD5不能为空");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new RuntimeException("文件大小错误");
        }

        return mediaFileMapper.selectByBucketMd5Size(bucketName, md5, fileSize);
    }

    public MediaFile getById(Long id) {
        if (id == null) {
            throw new RuntimeException("文件ID不能为空");
        }
        return mediaFileMapper.selectMediaFileById(id);
    }

    public MediaFile getByBucketObject(String bucketName, String objectName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new RuntimeException("bucket不能为空");
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new RuntimeException("objectName不能为空");
        }

        return mediaFileMapper.selectByBucketObject(bucketName, objectName);
    }

    @Override
    public List<Long> listNeedRetryTranscodeFileIds(Integer retryAfterMinutes, Integer limit) {
        return mediaFileMapper.listNeedRetryTranscodeFileIds(retryAfterMinutes, limit);
    }

}
