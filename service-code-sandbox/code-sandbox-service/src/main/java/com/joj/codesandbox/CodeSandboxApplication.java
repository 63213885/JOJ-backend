package com.joj.codesandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.joj")
public class CodeSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeSandboxApplication.class, args);
    }

}
