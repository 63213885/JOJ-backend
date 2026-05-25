package com.joj.media.model.dto;

import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:51
 */

@Data
public class InitUploadRequest {

    private String originalFilename;

    private String contentType;

    /**
     * 前端计算的整个文件 MD5
     */
    private String md5;

    private Long fileSize;

    private Long chunkSize;

    private Integer chunkCount;
}
