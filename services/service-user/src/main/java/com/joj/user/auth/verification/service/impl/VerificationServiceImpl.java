package com.joj.user.auth.verification.service.impl;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.user.auth.model.IdentifierType;
import com.joj.user.auth.verification.codeSender.CodeSender;
import com.joj.user.auth.verification.codeStore.VerificationCodeStore;
import com.joj.user.auth.verification.config.VerificationProperties;
import com.joj.user.auth.verification.model.SendCodeResult;
import com.joj.user.auth.verification.model.VerificationCheckResult;
import com.joj.user.auth.verification.model.VerificationScene;
import com.joj.user.auth.verification.service.VerificationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 16:53
 */

@Service
public class VerificationServiceImpl implements VerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private VerificationCodeStore codeStore;
    @Resource(name = "realCodeSender")
    private CodeSender codeSender;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private VerificationProperties properties;

    /**
     * 发送间隔限制：同一标识在指定间隔内只能发送一次。
     *
     * @param scene      验证码场景。
     * @param identifier 标识（手机号或邮箱）。
     * @param interval   发送间隔。
     */
    private void enforceSendInterval(VerificationScene scene, String identifier, Duration interval) {
        if (interval.isZero() || interval.isNegative()) {
            return;
        }
        String key = "verification:code:last:" + scene.name() + ":" + identifier;
        String existing = stringRedisTemplate.opsForValue().get(key);
        if (existing != null) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "请勿频繁发送验证码");
        }
        stringRedisTemplate.opsForValue().set(key, "1", interval);
    }

    /**
     * 每日发送次数限制：超过上限则抛出限额异常。
     *
     * @param scene      验证码场景。
     * @param identifier 标识（手机号或邮箱）。
     * @param limit      每日上限次数。
     */
    private void enforceDailyLimit(VerificationScene scene, String identifier, int limit) {
        if (limit <= 0) {
            return;
        }
        String date = DAY_FORMAT.format(LocalDate.now());
        String key = "verification:code:count:" + scene.name() + ":" + identifier + ":" + date;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // 计算到明天 00:00:00 的剩余时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrowStart = now.toLocalDate().plusDays(1).atStartOfDay();
            Duration ttl = Duration.between(now, tomorrowStart);
            stringRedisTemplate.expire(key, ttl);
        }
        if (count != null && count > limit) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "今日验证码发送次数已达上限");
        }
    }

    /**
     * 生成指定长度的纯数字验证码。
     *
     * @param length 验证码长度。
     * @return 数字字符串。
     */
    private static String generateNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    /**
     * 发送验证码到指定标识。
     * <p>
     * 执行发送间隔与日次数限制，生成随机数字验证码，保存到存储并调用发送器。
     *
     * @param scene      验证码场景（REGISTER/LOGIN/RESET_PASSWORD）。
     * @param identifier 标识（手机号或邮箱）。
     * @return 发送结果，包含标识、场景与过期秒数。
     * @throws BusinessException 参数不完整或触发速率/日限额时抛出。
     */
    public SendCodeResult sendCode(VerificationScene scene, IdentifierType identifierType, String identifier) {
        if (scene == null || !StringUtils.hasText(identifier)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请提供正确的验证码发送参数");
        }
        enforceSendInterval(scene, identifier, properties.getSendInterval());
        enforceDailyLimit(scene, identifier, properties.getDailyLimit());

        String code = generateNumericCode(properties.getCodeLength());
        codeSender.sendCode(scene, identifierType, identifier, code, (int) properties.getTtl().toMinutes());
        codeStore.saveCode(scene.name(), identifier, code, properties.getTtl(), properties.getMaxAttempts());
        return new SendCodeResult(identifier, scene, (int) properties.getTtl().getSeconds());
    }

    /**
     * 校验验证码是否正确且未超限。
     *
     * @param scene      验证码场景。
     * @param identifier 标识（手机号或邮箱）。
     * @param code       用户输入的验证码。
     * @return 校验结果，包含状态与尝试次数统计。
     * @throws BusinessException 参数不完整时抛出。
     */
    public VerificationCheckResult verify(VerificationScene scene, IdentifierType identifierType, String identifier, String code) {
        if (scene == null || !StringUtils.hasText(identifier) || !StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码校验参数不完整");
        }
        return codeStore.verify(scene.name(), identifier, code);
    }

}
