package com.joj.media.service.Impl;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.common.core.model.enums.MediaEncryptTypeEnum;
import com.joj.common.core.model.enums.MediaTranscodeStatusEnum;
import com.joj.common.core.model.vo.MediaVideoInfoVO;
import com.joj.media.service.MediaFileService;
import com.joj.media.service.MediaVideoService;
import com.joj.media.service.MinioObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 20:50
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaVideoServiceImpl implements MediaVideoService {

    private static final String HLS_INDEX_FILE_NAME = "index.m3u8";

    private static final int DEFAULT_SEGMENT_EXPIRE_SECONDS = 10 * 60;

    private final MediaFileService mediaFileService;

    private final MinioObjectService minioObjectService;




    private MediaFile getMediaFile(Long fileId) {
        if (fileId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID不能为空");
        }
        MediaFile mediaFile = mediaFileService.getById(fileId);
        if (mediaFile == null || Objects.equals(mediaFile.getIsDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "媒体文件不存在");
        }
        return mediaFile;
    }

    @Override
    public MediaVideoInfoVO getVideoInfo(Long fileId) {
        MediaFile mediaFile = getMediaFile(fileId);

        MediaVideoInfoVO vo = new MediaVideoInfoVO();
        vo.setFileId(mediaFile.getId());
        vo.setContentType(mediaFile.getContentType());
        vo.setTranscodeStatus(mediaFile.getTranscodeStatus());
        vo.setEncryptType(mediaFile.getEncryptType());
        vo.setHlsPrefix(mediaFile.getHlsPrefix());

        return vo;
    }

    private MediaFile getAndCheckHlsMediaFile(Long fileId) {
        MediaFile mediaFile = getMediaFile(fileId);

        if (!Objects.equals(mediaFile.getTranscodeStatus(), MediaTranscodeStatusEnum.SUCCESS.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "视频暂未转码完成");
        }

        if (!Objects.equals(mediaFile.getEncryptType(), MediaEncryptTypeEnum.HLS_AES_128_KEY_OBFUSCATED.getValue())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频加密类型异常");
        }

        if (mediaFile.getHlsPrefix() == null || mediaFile.getHlsPrefix().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS目录不存在");
        }

        return mediaFile;
    }

    @Override
    public String getHlsIndex(Long fileId) {
        MediaFile mediaFile = getAndCheckHlsMediaFile(fileId);

        String objectName = mediaFile.getHlsPrefix() + HLS_INDEX_FILE_NAME;

        return minioObjectService.getObjectAsString(
                mediaFile.getBucketName(),
                objectName
        );
    }


    private byte[] buildKeyMask(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String material = token + ":joj:hls:key:mask:v1";
            byte[] hash = digest.digest(material.getBytes(StandardCharsets.UTF_8));

            return Arrays.copyOf(hash, 16);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成 HLS key mask 失败");
        }
    }

    private byte[] obfuscateKey(byte[] realKey, String token) {
        if (realKey == null || realKey.length != 16) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS key 长度必须为 16 字节");
        }

        byte[] mask = buildKeyMask(token);

        byte[] result = new byte[16];
        for (int i = 0; i < 16; i++) {
            result[i] = (byte) (realKey[i] ^ mask[i]);
        }

        return result;
    }

    @Override
    public String getObfuscatedHlsKeyBase64(Long fileId, String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "播放 token 不能为空");
        }

        MediaFile mediaFile = getAndCheckHlsMediaFile(fileId);

        byte[] realKey = mediaFile.getEncryptKey();
        if (realKey == null || realKey.length != 16) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频密钥异常");
        }

        byte[] fakeKey = obfuscateKey(realKey, token);

        return Base64.getEncoder().encodeToString(fakeKey);
    }

    @Override
    public void writeHlsSegment(Long fileId, String segmentName, HttpServletResponse response) {
        if (fileId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID不能为空");
        }

        if (segmentName == null
                || segmentName.trim().isEmpty()
                || segmentName.contains("..")
                || segmentName.contains("/")
                || segmentName.contains("\\")
                || !segmentName.endsWith(".ts")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法视频分片名称");
        }

        MediaFile mediaFile = getMediaFile(fileId);
        if (mediaFile == null || Objects.equals(mediaFile.getIsDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "媒体文件不存在");
        }

        if (mediaFile.getBucketName() == null || mediaFile.getObjectName() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "媒体文件存储信息异常");
        }

        if (mediaFile.getHlsPrefix() == null || mediaFile.getHlsPrefix().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "视频尚未完成 HLS 转码");
        }

        String objectName = mediaFile.getHlsPrefix() + segmentName;

        minioObjectService.writeObjectToResponse(
                mediaFile.getBucketName(),
                objectName,
                "video/mp2t",
                response
        );
    }

}
