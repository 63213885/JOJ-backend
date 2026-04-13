package com.joj.user.auth.verification.model;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 17:42
 */

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 验证码校验结果。
 * <p>
 * 包含状态（成功/未找到/过期/错误/尝试过多）和次数统计信息，提供便捷成功判断。
 */
@Data
@AllArgsConstructor
public class VerificationCheckResult {

    public VerificationCodeStatus status;

    int attempts;

    int maxAttempts;

    public boolean isSuccess() {
        return status == VerificationCodeStatus.SUCCESS;
    }
}
