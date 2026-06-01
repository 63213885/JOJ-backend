package com.joj.course.service.impl;

import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.course.service.VideoPlayTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 18:08
 */

@Slf4j
@Service
public class VideoPlayTokenServiceImpl implements VideoPlayTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private static final String TOKEN_SEPARATOR = ".";

    private static final String PAYLOAD_SEPARATOR = ":";

    @Value("${video.play-token-secret}")
    private String secret;

    @Value("${video.play-token-expire-seconds:600}")
    private Long expireSeconds;

    @Override
    public String createToken(Long lessonId, Long fileId) {
        if (lessonId == null || fileId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "播放参数不能为空");
        }

        Long userId = getLoginUserId();

        long expireAt = System.currentTimeMillis() + expireSeconds * 1000L;

        String nonce = UUID.randomUUID().toString().replace("-", "");

        String payload = userId
                + PAYLOAD_SEPARATOR + lessonId
                + PAYLOAD_SEPARATOR + fileId
                + PAYLOAD_SEPARATOR + expireAt
                + PAYLOAD_SEPARATOR + nonce;

        String payloadBase64 = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));

        byte[] signBytes = hmacSha256(payload);
        String signBase64 = base64UrlEncode(signBytes);

        return payloadBase64 + TOKEN_SEPARATOR + signBase64;
    }

    @Override
    public void checkToken(String token, Long lessonId, Long fileId) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 不能为空");
        }

        if (lessonId == null || fileId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "播放参数不能为空");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 格式错误");
        }

        String payloadBase64 = parts[0];
        String signBase64 = parts[1];

        String payload;
        try {
            payload = new String(base64UrlDecode(payloadBase64), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 解析失败");
        }

        byte[] expectedSign = hmacSha256(payload);
        byte[] actualSign;

        try {
            actualSign = base64UrlDecode(signBase64);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 签名解析失败");
        }

        if (!MessageDigest.isEqual(expectedSign, actualSign)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 签名错误");
        }

        String[] payloadParts = payload.split(PAYLOAD_SEPARATOR);
        if (payloadParts.length != 5) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 内容错误");
        }

        Long tokenUserId;
        Long tokenLessonId;
        Long tokenFileId;
        Long expireAt;

        try {
            tokenUserId = Long.valueOf(payloadParts[0]);
            tokenLessonId = Long.valueOf(payloadParts[1]);
            tokenFileId = Long.valueOf(payloadParts[2]);
            expireAt = Long.valueOf(payloadParts[3]);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 内容解析失败");
        }

        Long currentUserId = getLoginUserId();

        if (!Objects.equals(tokenUserId, currentUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 用户不匹配");
        }

        if (!Objects.equals(tokenLessonId, lessonId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 课时不匹配");
        }

        if (!Objects.equals(tokenFileId, fileId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 文件不匹配");
        }

        if (System.currentTimeMillis() > expireAt) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "播放 token 已过期");
        }
    }

    private Long getLoginUserId() {
        if (UserContext.get() == null || UserContext.get().getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        return UserContext.get().getId();
    }

    private byte[] hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成播放 token 签名失败");
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
