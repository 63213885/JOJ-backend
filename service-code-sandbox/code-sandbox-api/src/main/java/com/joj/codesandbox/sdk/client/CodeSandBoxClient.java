package com.joj.codesandbox.sdk.client;

import com.joj.codesandbox.sdk.dto.CompileRequest;
import com.joj.codesandbox.sdk.dto.CompileResponse;
import com.joj.codesandbox.sdk.dto.RunRequest;
import com.joj.codesandbox.sdk.dto.RunResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/20 19:10
 */

@FeignClient(name = "service-code-sandbox", path = "/api/codeSandbox")
public interface CodeSandBoxClient {

    @PostMapping("/compile")
    CompileResponse compile(@RequestBody CompileRequest compileRequest, @RequestHeader("auth") String auth);

    @PostMapping("/run")
    RunResponse run(@RequestBody RunRequest runRequest, @RequestHeader("auth") String auth);

    @DeleteMapping("/{sandboxId}")
    void deleteFile(@PathVariable String sandboxId, @RequestHeader("auth") String auth);

}
