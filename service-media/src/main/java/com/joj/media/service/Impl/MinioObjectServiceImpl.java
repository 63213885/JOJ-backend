package com.joj.media.service.Impl;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.media.service.MinioObjectService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 15:08
 */

@Service
@RequiredArgsConstructor
public class MinioObjectServiceImpl implements MinioObjectService {

    private final MinioClient minioClient;

    @Override
    public void putObject(MultipartFile file, String bucketName, String objectName, String contentType) {
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件到 MinIO 失败");
        }
    }

    @Override
    public void uploadLocalFile(String bucketName, String objectName, String localFilePath, String contentType) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localFilePath)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传本地文件到 MinIO 失败");
        }
    }

    @Override
    public void downloadObject(String bucketName, String objectName, String localFilePath) {
        try {
            File localFile = new File(localFilePath);
            File parentFile = localFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
                if (!mkdirs) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建本地目录失败");
                }
            }

            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localFilePath)
                            .build()
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "从 MinIO 下载文件失败");
        }
    }

    @Override
    public String getObjectAsString(String bucketName, String objectName) {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取 MinIO 文件失败");
        }
    }

    @Override
    public String getPresignedGetUrl(String bucketName, String objectName, int expireSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expireSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成临时访问地址失败");
        }
    }



    @Override
    public void removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除 MinIO 文件失败");
        }
    }

    @Override
    public void writeObjectToResponse(String bucketName, String objectName, String contentType, HttpServletResponse response) {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        )) {
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "private, max-age=60");

            ServletOutputStream outputStream = response.getOutputStream();

            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取 MinIO 文件失败");
        }
    }

}
