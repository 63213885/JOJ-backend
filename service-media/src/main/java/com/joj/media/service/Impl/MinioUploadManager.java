package com.joj.media.service.Impl;


/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 17:03
 */

import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.enums.MediaAccessTypeEnum;
import com.joj.common.core.model.enums.MediaEncryptTypeEnum;
import com.joj.common.core.model.enums.MediaTranscodeStatusEnum;
import com.joj.media.config.MinioProperties;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.media.service.MediaFileService;
import com.joj.media.service.MinioObjectService;
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
    private MinioObjectService minioObjectService;

    @Resource
    private MediaFileService mediaFileService;


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

    private void uploadAndRecord(MultipartFile file,
                                 String bucketName,
                                 String objectName,
                                 FileCheckResult checkResult,
                                 Integer accessType,
                                 Integer transcodeStatus) {
        boolean uploaded = false;
        try {
            MediaFile existFile = mediaFileService.getByBucketObject(bucketName, objectName);
            if (existFile != null) {
                return;
            }

            minioObjectService.putObject(
                    file,
                    bucketName,
                    objectName,
                    checkResult.getContentType()
            );
            uploaded = true;

            mediaFileService.addMediaFile(
                    MediaFile.builder()
                            .originalFilename(checkResult.getOriginalFilename())
                            .contentType(checkResult.getContentType())
                            .md5(checkResult.getMd5())
                            .fileSize(checkResult.getFileSize())
                            .bucketName(bucketName)
                            .objectName(objectName)
                            .accessType(accessType)
                            .transcodeStatus(transcodeStatus)
                            .encryptType(MediaEncryptTypeEnum.NONE.getValue())
                            .creatorId(UserContext.get().getId())
                            .build()
            );

        } catch (Exception e) {
            if (uploaded) {
                try {
                    minioObjectService.removeObject(bucketName, objectName);
                } catch (Exception ignored) {
                }
            }

            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
    }

    /**
     * 上传用户头像，返回公开访问 URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile file) {
        FileCheckResult checkResult = checkFile(file, AVATAR_MAX_SIZE, IMAGE_TYPE_MAP);

        String bucketName = minioProperties.getBucket().getImage();
        String objectName = buildObjectName("avatar", checkResult);

        uploadAndRecord(
                file,
                bucketName,
                objectName,
                checkResult,
                MediaAccessTypeEnum.PUBLIC.getValue(),
                MediaTranscodeStatusEnum.NO_NEED.getValue()
        );

        return minioProperties.getBucket().getImage() + "/" + objectName;
    }

    /**
     * 上传课程头像，返回公开访问 URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String uploadCover(MultipartFile file) {
        FileCheckResult checkResult = checkFile(file, COURSE_COVER_MAX_SIZE, IMAGE_TYPE_MAP);
        String bucketName = minioProperties.getBucket().getImage();
        String objectName = buildObjectName("cover", checkResult);

        uploadAndRecord(
                file,
                bucketName,
                objectName,
                checkResult,
                MediaAccessTypeEnum.PUBLIC.getValue(),
                MediaTranscodeStatusEnum.NO_NEED.getValue()
        );

        return minioProperties.getBucket().getImage() + "/" + objectName;
    }

}