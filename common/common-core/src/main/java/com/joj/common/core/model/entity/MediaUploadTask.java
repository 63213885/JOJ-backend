package com.joj.common.core.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:53
 */

@Data
public class MediaUploadTask {

    private Long id;

    private String uploadId;

    private String bucketName;

    private String objectName;

    private String originalFilename;

    private String contentType;

    private String md5;

    private Long fileSize;

    private Long chunkSize;

    private Integer chunkCount;

    private Integer accessType;

    private Long creatorId;

    /**
     * 0上传中 1已完成 2已取消 3失败
     */
    private Integer status;

    private Long mediaFileId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}