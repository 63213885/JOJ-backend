package com.joj.media.service;


/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 17:03
 */

import com.joj.api.UserFeignClient;
import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.media.config.MinioProperties;
import com.joj.common.core.model.entity.MediaFile;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class MinioUploadManager {

    @Resource
    private MinioProperties minioProperties;
    @Resource
    private MinioClient minioClient;

    @Resource
    private MediaFileService mediaFileService;
    @Resource
    private UserFeignClient userFeignClient;


    private static final Tika TIKA = new Tika();

    private static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024L;
    private static final long COURSE_COVER_MAX_SIZE = 5 * 1024 * 1024L;
    private static final long COURSE_VIDEO_MAX_SIZE = 1024 * 1024 * 1024L;
    private static final long TESTCASE_MAX_SIZE = 100 * 1024 * 1024L;

    private static final Map<String, List<String>> IMAGE_TYPE_MAP = new HashMap<>();
    private static final Map<String, List<String>> VIDEO_TYPE_MAP = new HashMap<>();
    private static final Map<String, List<String>> TESTCASE_TYPE_MAP = new HashMap<>();

    static {
        IMAGE_TYPE_MAP.put("jpg", Collections.singletonList("image/jpeg"));
        IMAGE_TYPE_MAP.put("jpeg", Collections.singletonList("image/jpeg"));
        IMAGE_TYPE_MAP.put("png", Collections.singletonList("image/png"));
        IMAGE_TYPE_MAP.put("webp", Collections.singletonList("image/webp"));

        VIDEO_TYPE_MAP.put("mp4", Collections.singletonList("video/mp4"));

        TESTCASE_TYPE_MAP.put("zip", Arrays.asList(
                "application/zip",
                "application/x-zip-compressed",
                "application/x-zip"
        ));
    }

    @Data
    @Builder
    private static class FileCheckResult {

        private String originalFilename;

        private String contentType;

        private String md5;

        private Long fileSize;

        private String suffix;

    }

    private String getSuffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("文件后缀错误");
        }

        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String detectContentType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return TIKA.detect(inputStream, file.getOriginalFilename());
        } catch (Exception e) {
            throw new RuntimeException("检测文件类型失败", e);
        }
    }

    private String getMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("计算文件MD5失败", e);
        }
    }

    private FileCheckResult checkFile(MultipartFile file, long maxSize, Map<String, List<String>> allowTypeMap) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小超出限制");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = getSuffix(originalFilename);

        List<String> allowContentTypes = allowTypeMap.get(suffix);
        if (allowContentTypes == null) {
            throw new RuntimeException("文件后缀不支持");
        }

        String realContentType = detectContentType(file);
        if (!allowContentTypes.contains(realContentType)) {
            throw new RuntimeException("文件后缀和真实类型不匹配");
        }

        String md5 = getMd5(file);

        return FileCheckResult.builder()
                .originalFilename(originalFilename)
                .suffix(suffix)
                .contentType(realContentType)
                .md5(md5)
                .fileSize(file.getSize())
                .build();
    }

    private String buildObjectName(String prefix, FileCheckResult checkResult) {
        return prefix + "/" + checkResult.getMd5() + "_" + checkResult.getFileSize() + "." + checkResult.getSuffix();
    }

    private void upload(MultipartFile file, String bucketName, String objectName, String contentType) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传文件到 MinIO 失败", e);
        }
    }

    private void uploadAndRecord(MultipartFile file, String bucketName, String objectName, FileCheckResult checkResult, Integer accessType) {
        try {
            if (mediaFileService.getByBucketObject(bucketName, objectName) == null) {

                upload(file, bucketName, objectName, checkResult.getContentType());

                mediaFileService.addMediaFile(
                        MediaFile.builder()
                                .originalFilename(checkResult.getOriginalFilename())
                                .contentType(checkResult.getContentType())
                                .md5(checkResult.getMd5())
                                .fileSize(checkResult.getFileSize())
                                .bucketName(bucketName)
                                .objectName(objectName)
                                .accessType(accessType)
                                .creatorId(UserContext.get().getId())
                                .build()
                );
            }
//            userFeignClient.updateAvatar(UserContext.get().getId(), minioProperties.getBucket().getImage() + "/" + objectName);

        } catch (Exception e) {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                );
            } catch (Exception ignored) {
            }
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传头像，返回公开访问 URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile file) {
        FileCheckResult checkResult = checkFile(file, AVATAR_MAX_SIZE, IMAGE_TYPE_MAP);

        String bucketName = minioProperties.getBucket().getImage();
        String objectName = buildObjectName("avatar", checkResult);

        uploadAndRecord(file, bucketName, objectName, checkResult, 1);

        return minioProperties.getBucket().getImage() + "/" + objectName;
    }

    @Transactional(rollbackFor = Exception.class)
    public String uploadCover(MultipartFile file) {
        FileCheckResult checkResult = checkFile(file, COURSE_COVER_MAX_SIZE, IMAGE_TYPE_MAP);
        String bucketName = minioProperties.getBucket().getImage();
        String objectName = buildObjectName("cover", checkResult);

        uploadAndRecord(file, bucketName, objectName, checkResult, 1);

        return minioProperties.getBucket().getImage() + "/" + objectName;
    }

    public String getPresignedGetUrl(String bucket, String objectKey, int expireSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expireSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成视频播放地址失败");
        }
    }

}