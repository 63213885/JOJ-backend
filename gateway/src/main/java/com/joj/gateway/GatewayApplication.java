package com.joj.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/14 13:25
 */

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
        "com.joj"
})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}

/*

本地集群：
编辑配置 -> 修改选项 -> 程序实参 -> --server.port=18083

服务运行成功但是JMX连接失败：
编辑配置 -> 修改选项 -> 添加虚拟机选项 -> -Djava.rmi.server.hostname=127.0.0.1 -Djava.net.preferIPv4Stack=true

 */
