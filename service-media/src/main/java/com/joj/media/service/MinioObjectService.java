package com.joj.media.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 15:04
 */

public interface MinioObjectService {

    void putObject(MultipartFile file, String bucketName, String objectName, String contentType);

    void uploadLocalFile(String bucketName, String objectName, String localFilePath, String contentType);

    void downloadObject(String bucketName, String objectName, String localFilePath);

    String getObjectAsString(String bucketName, String objectName);

    String getPresignedGetUrl(String bucketName, String objectName, int expireSeconds);

    void removeObject(String bucketName, String objectName);

    void writeObjectToResponse(String bucketName, String objectName, String contentType, HttpServletResponse response);
}
