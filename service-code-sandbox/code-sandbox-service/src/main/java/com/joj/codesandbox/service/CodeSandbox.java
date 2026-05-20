package com.joj.codesandbox.service;


import com.joj.codesandbox.sdk.dto.CompileRequest;
import com.joj.codesandbox.sdk.dto.CompileResponse;
import com.joj.codesandbox.sdk.dto.RunRequest;
import com.joj.codesandbox.sdk.dto.RunResponse;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 19:00
 */

public interface CodeSandbox {

    CompileResponse compile(CompileRequest compileRequest);

    RunResponse run(RunRequest runRequest);

    void deleteFile(String sandboxId);

}
