package com.joj.media.service.Impl;

import com.joj.common.core.model.entity.MediaUploadPart;
import com.joj.media.config.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:54
 */

@Component
@RequiredArgsConstructor
public class S3MultipartUploadManager {

    private final MinioProperties minioProperties;

    private S3Client s3Client;

    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey()
        );

        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(minioProperties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.US_EAST_1)
                .serviceConfiguration(s3Configuration)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(minioProperties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.US_EAST_1)
                .serviceConfiguration(s3Configuration)
                .build();
    }

    public String createMultipartUpload(String bucketName, String objectName, String contentType) {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .contentType(contentType)
                .build();

        return s3Client.createMultipartUpload(request).uploadId();
    }

    public String presignUploadPart(String bucketName,
                                    String objectName,
                                    String uploadId,
                                    Integer partNumber) {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .uploadPartRequest(uploadPartRequest)
                .build();

        PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);

        return presignedRequest.url().toString();
    }

    public void completeMultipartUpload(String bucketName,
                                        String objectName,
                                        String uploadId,
                                        List<CompletedPart> completedParts) {
        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .uploadId(uploadId)
                .multipartUpload(multipartUpload)
                .build();

        s3Client.completeMultipartUpload(request);
    }

    public void abortMultipartUpload(String bucketName, String objectName, String uploadId) {
        AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .uploadId(uploadId)
                .build();

        s3Client.abortMultipartUpload(request);
    }

    public List<CompletedPart> toCompletedParts(List<MediaUploadPart> partList) {
        return partList.stream()
                .map(part -> CompletedPart.builder()
                        .partNumber(part.getPartNumber())
                        .eTag(part.getEtag())
                        .build())
                .collect(Collectors.toList());
    }
}
