package com.joj.user.auth.verification.model;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 17:41
 */

// 分别对应：成功、找不到、过期、不匹配、尝试次数过多
public enum VerificationCodeStatus {
    SUCCESS,
    NOT_FOUND,
    EXPIRED,
    MISMATCH,
    TOO_MANY_ATTEMPTS
}
