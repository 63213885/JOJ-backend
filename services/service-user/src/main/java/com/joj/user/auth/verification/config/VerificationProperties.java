package com.joj.user.auth.verification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 20:38
 */

@Data
@Component
@ConfigurationProperties(prefix = "verification")
public class VerificationProperties {

    private Integer codeLength;
    private Duration ttl;
    private Integer maxAttempts;
    private Duration sendInterval;
    private Integer dailyLimit;
}
