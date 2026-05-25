package com.joj.common.core.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:54
 */

@Data
public class MediaUploadPart {

    private Long id;

    private Long taskId;

    private String uploadId;

    private Integer partNumber;

    private String etag;

    private Long partSize;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
