package com.joj.media.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:52
 */

@Data
@Builder
public class InitUploadVO {

    private Long taskId;

    private String uploadId;

    private String bucketName;

    private String objectName;

    private Long chunkSize;

    private Integer chunkCount;

    /**
     * 已上传分片编号，用于断点续传
     */
    private List<Integer> uploadedPartNumbers;

    /**
     * 是否秒传
     */
    private Boolean finished;

    /**
     * 秒传时返回已有 mediaFileId
     */
    private Long mediaFileId;
}
