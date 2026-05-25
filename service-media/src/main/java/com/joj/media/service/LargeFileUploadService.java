package com.joj.media.service;

import com.joj.media.model.dto.*;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:56
 */

public interface LargeFileUploadService {

    InitUploadVO initUpload(InitUploadRequest request);

    UploadPartUrlVO getPartUploadUrl(Long taskId, Integer partNumber);

    Boolean recordPart(RecordUploadPartRequest request);

    List<UploadedPartVO> listUploadedParts(Long taskId);

    Long completeUpload(CompleteUploadRequest request);

    Boolean abortUpload(Long taskId);
}
