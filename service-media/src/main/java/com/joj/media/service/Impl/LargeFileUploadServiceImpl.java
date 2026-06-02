package com.joj.media.service.Impl;

import com.joj.common.core.context.UserContext;
import com.joj.common.core.model.enums.MediaEncryptTypeEnum;
import com.joj.common.core.model.enums.MediaTranscodeStatusEnum;
import com.joj.common.core.model.enums.MediaUploadTaskStatus;
import com.joj.media.config.MinioProperties;
import com.joj.media.mapper.MediaFileMapper;
import com.joj.media.mapper.MediaUploadPartMapper;
import com.joj.media.mapper.MediaUploadTaskMapper;
import com.joj.media.model.dto.*;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.common.core.model.entity.MediaUploadPart;
import com.joj.common.core.model.entity.MediaUploadTask;
import com.joj.media.mq.VideoTranscodeProducer;
import com.joj.media.service.LargeFileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:57
 */

@Service
@RequiredArgsConstructor
public class LargeFileUploadServiceImpl implements LargeFileUploadService {

    private final MinioProperties minioProperties;

    private final S3MultipartUploadManager s3MultipartUploadManager;

    private final MediaFileMapper mediaFileMapper;

    private final MediaUploadTaskMapper mediaUploadTaskMapper;

    private final MediaUploadPartMapper mediaUploadPartMapper;

    private final VideoTranscodeProducer videoTranscodeProducer;


    private static final int ACCESS_PRIVATE = 0;


    private void checkInitRequest(InitUploadRequest request) {
        if (request == null) {
            throw new RuntimeException("请求不能为空");
        }
        if (request.getOriginalFilename() == null || request.getOriginalFilename().trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        if (request.getContentType() == null || !request.getContentType().equals("video/mp4")) {
            throw new RuntimeException("只支持mp4视频");
        }
        if (request.getMd5() == null || request.getMd5().length() != 32) {
            throw new RuntimeException("文件MD5错误");
        }
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw new RuntimeException("文件大小错误");
        }
        if (request.getChunkSize() == null || request.getChunkSize() < 5 * 1024 * 1024L) {
            throw new RuntimeException("分片大小不能小于5MB");
        }
        if (request.getChunkCount() == null || request.getChunkCount() <= 0) {
            throw new RuntimeException("分片总数错误");
        }

        String suffix = getSuffix(request.getOriginalFilename());
        if (!"mp4".equals(suffix)) {
            throw new RuntimeException("只支持mp4视频");
        }
    }

    private String buildVideoObjectName(String md5, Long fileSize, String suffix) {
        return "course/video/" + md5 + "_" + fileSize + "." + suffix;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InitUploadVO initUpload(InitUploadRequest request) {
        checkInitRequest(request);

        String bucketName = minioProperties.getBucket().getVideo();
        String suffix = getSuffix(request.getOriginalFilename());
        String objectName = buildVideoObjectName(request.getMd5(), request.getFileSize(), suffix);
        Long creatorId = UserContext.get() == null ? null : UserContext.get().getId();

        MediaFile existedFile = mediaFileMapper.selectByBucketObject(bucketName, objectName);
        if (existedFile != null) {
            return InitUploadVO.builder()
                    .finished(true)
                    .mediaFileId(existedFile.getId())
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .uploadedPartNumbers(null)
                    .build();
        }

        MediaUploadTask existedTask = mediaUploadTaskMapper.selectUploadingTask(bucketName, request.getMd5(), request.getFileSize());
        if (existedTask != null) {
            List<Integer> uploadedPartNumbers = mediaUploadPartMapper.listUploadedPartNumbers(existedTask.getId());

            return InitUploadVO.builder()
                    .finished(false)
                    .taskId(existedTask.getId())
                    .uploadId(existedTask.getUploadId())
                    .bucketName(existedTask.getBucketName())
                    .objectName(existedTask.getObjectName())
                    .chunkSize(existedTask.getChunkSize())
                    .chunkCount(existedTask.getChunkCount())
                    .uploadedPartNumbers(uploadedPartNumbers)
                    .build();
        }

        String uploadId = s3MultipartUploadManager.createMultipartUpload(
                bucketName,
                objectName,
                request.getContentType()
        );

        MediaUploadTask task = new MediaUploadTask();
        task.setUploadId(uploadId);
        task.setBucketName(bucketName);
        task.setObjectName(objectName);
        task.setOriginalFilename(request.getOriginalFilename());
        task.setContentType(request.getContentType());
        task.setMd5(request.getMd5());
        task.setFileSize(request.getFileSize());
        task.setChunkSize(request.getChunkSize());
        task.setChunkCount(request.getChunkCount());
        task.setAccessType(ACCESS_PRIVATE);
        task.setCreatorId(creatorId);
        task.setStatus(MediaUploadTaskStatus.UPLOADING.getValue());

        mediaUploadTaskMapper.insertUploadTask(task);

        return InitUploadVO.builder()
                .finished(false)
                .taskId(task.getId())
                .uploadId(uploadId)
                .bucketName(bucketName)
                .objectName(objectName)
                .chunkSize(task.getChunkSize())
                .chunkCount(task.getChunkCount())
                .uploadedPartNumbers(null)
                .build();
    }

    @Override
    public UploadPartUrlVO getPartUploadUrl(Long taskId, Integer partNumber) {
        if (taskId == null) {
            throw new RuntimeException("上传任务ID不能为空");
        }
        if (partNumber == null || partNumber <= 0) {
            throw new RuntimeException("分片编号错误");
        }

        MediaUploadTask task = mediaUploadTaskMapper.selectById(taskId);
        if (task == null || !MediaUploadTaskStatus.UPLOADING.getValue().equals(task.getStatus())) {
            throw new RuntimeException("上传任务不存在或状态错误");
        }
        if (partNumber > task.getChunkCount()) {
            throw new RuntimeException("分片编号超出范围");
        }

        String uploadUrl = s3MultipartUploadManager.presignUploadPart(
                task.getBucketName(),
                task.getObjectName(),
                task.getUploadId(),
                partNumber
        );

        return UploadPartUrlVO.builder()
                .taskId(task.getId())
                .uploadId(task.getUploadId())
                .partNumber(partNumber)
                .uploadUrl(uploadUrl)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recordPart(RecordUploadPartRequest request) {
        if (request.getTaskId() == null) {
            throw new RuntimeException("上传任务ID不能为空");
        }
        if (request.getPartNumber() == null || request.getPartNumber() <= 0) {
            throw new RuntimeException("分片编号错误");
        }
        if (request.getEtag() == null || request.getEtag().trim().isEmpty()) {
            throw new RuntimeException("ETag不能为空");
        }
        if (request.getPartSize() == null || request.getPartSize() <= 0) {
            throw new RuntimeException("分片大小错误");
        }

        MediaUploadTask task = mediaUploadTaskMapper.selectById(request.getTaskId());
        if (task == null || !MediaUploadTaskStatus.UPLOADING.getValue().equals(task.getStatus())) {
            throw new RuntimeException("上传任务不存在或状态错误");
        }

        MediaUploadPart part = new MediaUploadPart();
        part.setTaskId(task.getId());
        part.setUploadId(task.getUploadId());
        part.setPartNumber(request.getPartNumber());
        part.setEtag(cleanEtag(request.getEtag()));
        part.setPartSize(request.getPartSize());

        mediaUploadPartMapper.insertOrUpdatePart(part);
        return true;
    }

    @Override
    public List<UploadedPartVO> listUploadedParts(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("上传任务ID不能为空");
        }

        return mediaUploadPartMapper.listByTaskId(taskId)
                .stream()
                .map(part -> UploadedPartVO.builder()
                        .partNumber(part.getPartNumber())
                        .etag(part.getEtag())
                        .partSize(part.getPartSize())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long completeUpload(CompleteUploadRequest request) {
        if (request.getTaskId() == null) {
            throw new RuntimeException("上传任务ID不能为空");
        }

        MediaUploadTask task = mediaUploadTaskMapper.selectById(request.getTaskId());
        if (task == null || !MediaUploadTaskStatus.UPLOADING.getValue().equals(task.getStatus())) {
            throw new RuntimeException("上传任务不存在或状态错误");
        }

        List<MediaUploadPart> partList = mediaUploadPartMapper.listByTaskId(task.getId());
        if (partList.size() != task.getChunkCount()) {
            throw new RuntimeException("分片未全部上传完成");
        }

        List<CompletedPart> completedParts = s3MultipartUploadManager.toCompletedParts(partList);

        s3MultipartUploadManager.completeMultipartUpload(
                task.getBucketName(),
                task.getObjectName(),
                task.getUploadId(),
                completedParts
        );

        MediaFile mediaFile = new MediaFile();
        mediaFile.setOriginalFilename(task.getOriginalFilename());
        mediaFile.setContentType(task.getContentType());
        mediaFile.setMd5(task.getMd5());
        mediaFile.setFileSize(task.getFileSize());
        mediaFile.setBucketName(task.getBucketName());
        mediaFile.setObjectName(task.getObjectName());
        mediaFile.setAccessType(task.getAccessType());
        mediaFile.setCreatorId(task.getCreatorId());

        mediaFile.setTranscodeStatus(MediaTranscodeStatusEnum.WAITING.getValue());
        mediaFile.setEncryptType(MediaEncryptTypeEnum.NONE.getValue());

        try {
            mediaFileMapper.insertMediaFile(mediaFile);
        } catch (Exception e) {
            MediaFile existedFile = mediaFileMapper.selectByBucketObject(task.getBucketName(), task.getObjectName());
            if (existedFile == null) {
                throw e;
            }
            mediaFile = existedFile;
        }

        mediaUploadTaskMapper.updateStatusAndMediaFileId(task.getId(), MediaUploadTaskStatus.FINISHED.getValue(), mediaFile.getId());

        MediaFile finalMediaFile = mediaFile;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
//                videoTranscodeProducer.send(finalMediaFile.getId());
            }
        });

        return mediaFile.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean abortUpload(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("上传任务ID不能为空");
        }

        MediaUploadTask task = mediaUploadTaskMapper.selectById(taskId);
        if (task == null) {
            return true;
        }
        if (!MediaUploadTaskStatus.UPLOADING.getValue().equals(task.getStatus())) {
            return true;
        }

        s3MultipartUploadManager.abortMultipartUpload(
                task.getBucketName(),
                task.getObjectName(),
                task.getUploadId()
        );

        mediaUploadTaskMapper.updateStatus(taskId, MediaUploadTaskStatus.ABORTED.getValue());
        return true;
    }

    private String getSuffix(String filename) {
        int index = filename.lastIndexOf(".");
        if (index < 0 || index == filename.length() - 1) {
            throw new RuntimeException("文件后缀错误");
        }
        return filename.substring(index + 1).toLowerCase();
    }

    private String cleanEtag(String etag) {
        return etag.replace("\"", "");
    }
}
