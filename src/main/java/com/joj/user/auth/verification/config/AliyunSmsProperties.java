package com.joj.user.auth.verification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/10 20:51
 */

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String endpoint;

    private String signName;

    private String templateCode;
}
