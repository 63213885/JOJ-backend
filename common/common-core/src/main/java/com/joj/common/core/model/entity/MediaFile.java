package com.joj.common.core.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 22:45
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {

    private Long id;

    private String originalFilename;

    private String contentType;

    private String md5;

    private Long fileSize;

    private String bucketName;

    private String objectName;

    /**
     * 0 私有，1 公开
     */
    private Integer accessType;

    private Long creatorId;

    private Integer transcodeStatus;

    private String hlsPrefix;

    private Integer encryptType;

    private byte[] encryptKey;

    private String encryptIv;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
