package com.joj.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 16:57
 */

@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;

    private String publicEndpoint;

    private String accessKey;

    private String secretKey;

    private Bucket bucket = new Bucket();

    @Data
    public static class Bucket {
        private String image;
        private String video;
        private String testcase;
        private String document;
    }
}
