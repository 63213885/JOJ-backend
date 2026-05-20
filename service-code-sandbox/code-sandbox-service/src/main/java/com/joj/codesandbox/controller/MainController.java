package com.joj.codesandbox.controller;


import com.joj.codesandbox.model.LanguageConfig;
import com.joj.codesandbox.sdk.dto.CompileRequest;
import com.joj.codesandbox.sdk.dto.CompileResponse;
import com.joj.codesandbox.sdk.dto.RunRequest;
import com.joj.codesandbox.sdk.dto.RunResponse;
import com.joj.codesandbox.service.impl.DockerCodeSandbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 18:44
 */

@Slf4j
@RestController
@RequestMapping("/codeSandbox")
public class MainController {

    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "jzz";

    @Resource
    private DockerCodeSandbox codeSandbox;

    /*
    编译成功   true
    编译失败   false

    运行成功   true (可能 运行超时 内存超限，由调用方手动判断)
    运行超时   false
    内存超限   false
    输出超限   false
    运行时错误 false
    系统错误   false
    */

    @PostMapping("/compile")
    public CompileResponse compile(@RequestBody CompileRequest compileRequest, HttpServletRequest request, HttpServletResponse response) {
        log.info("收到编译请求: {}", compileRequest);
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        // 校验请求对象
        if (compileRequest == null
                || !StringUtils.hasText(compileRequest.getLanguage())
                || !StringUtils.hasText(compileRequest.getCode())
                || compileRequest.getTimeLimit() == null) {
            return CompileResponse.builder()
                    .success(false)
                    .message("编译请求的语言或代码不能为空")
                    .build();
        }
        if (LanguageConfig.getConfig(compileRequest.getLanguage(), compileRequest.getTimeLimit()) == null) {
            return CompileResponse.builder()
                    .success(false)
                    .message("不支持的语言")
                    .build();
        }

        return codeSandbox.compile(compileRequest);
    }

    @PostMapping("/run")
    public RunResponse run(@RequestBody RunRequest runRequest, HttpServletRequest request, HttpServletResponse response) {
        log.info("收到运行请求: {}", runRequest);
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }

        if (runRequest == null || !StringUtils.hasText(runRequest.getSandboxId())) {
            return RunResponse.builder()
                    .success(false)
                    .message("sandboxId 不能为空")
                    .build();
        }

        return codeSandbox.run(runRequest);
    }

    @DeleteMapping("/{sandboxId}")
    public void deleteFile(@PathVariable String sandboxId, HttpServletRequest request, HttpServletResponse response) {
        log.info("删除: {}", sandboxId);
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return;
        }
        // sandboxId 为空，直接忽略
        if (!StringUtils.hasText(sandboxId)) {
            return;
        }
        codeSandbox.deleteFile(sandboxId);
    }

}
