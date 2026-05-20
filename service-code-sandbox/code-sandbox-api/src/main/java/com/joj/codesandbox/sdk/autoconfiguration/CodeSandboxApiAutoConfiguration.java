package com.joj.codesandbox.sdk.autoconfiguration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/20 19:26
 */

@Configuration
@EnableFeignClients(basePackages = "com.joj.codesandbox.sdk.client")
public class CodeSandboxApiAutoConfiguration {
}
