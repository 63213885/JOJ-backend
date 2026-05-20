package com.joj.problem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/13 14:54
 */

@EnableFeignClients(basePackages = "com.joj.api")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.joj")
public class ProblemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProblemApplication.class, args);
    }

}
