package com.joj.judge.codeSandBox.service.impl;

import com.joj.common.core.model.enums.RunResultEnum;
import com.joj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.joj.judge.codeSandBox.model.ExecuteCodeResponse;
import com.joj.judge.codeSandBox.service.CodeSandBox;
import com.joj.judge.listener.CodeSandboxExecuteListener;
import com.joj.judge.model.CompileRequest;
import com.joj.judge.model.CompileResponse;
import com.joj.judge.model.RunRequest;
import com.joj.judge.model.RunResponse;

import com.joj.judge.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 14:13
 */

@Slf4j
public class RemoteCodeSandBox implements CodeSandBox {

    private final String baseUrl = "http://localhost:5050/codeSandbox";

    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "jzz";


    private CompileResponse compile(CompileRequest request) {
        return HttpUtils.post(
                baseUrl + "/compile",
                request,
                HttpUtils.headers(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET),
                CompileResponse.class
        );
    }

    private RunResponse run(RunRequest request) {
        return HttpUtils.post(
                baseUrl + "/run",
                request,
                HttpUtils.headers(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET),
                RunResponse.class
        );
    }

    private Void deleteFile(String sandboxId) {
        return HttpUtils.delete(
                baseUrl + "/" + sandboxId,
                HttpUtils.headers(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET),
                Void.class
        );
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, CodeSandboxExecuteListener listener) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        Integer timeLimit = executeCodeRequest.getTimeLimit();
        Integer memoryLimit = executeCodeRequest.getMemoryLimit();
        log.info("execute code, language: {}, timeLimit: {}, memoryLimit: {}", language, timeLimit, memoryLimit);

        String codeSandboxId = null;
        try {
            Boolean ok = listener.onCompiling();
            if (!Boolean.TRUE.equals(ok)) {
                return null;
            }

            CompileResponse compileResponse = compile(
                    CompileRequest.builder()
                            .code(code)
                            .language(language)
                            .timeLimit(timeLimit)
                            .build()
            );
            if (compileResponse == null) {
                listener.onSystemError();
                return null;
            }
            if (!compileResponse.getSuccess()) {
                log.info("message: {}", compileResponse.getMessage());
                listener.onCompileError();
                return null;
            }
            if (compileResponse.getSandboxId() == null) {
                listener.onSystemError();
                return null;
            }
            codeSandboxId = compileResponse.getSandboxId();

            ok = listener.onRunning();
            if (!Boolean.TRUE.equals(ok)) {
                return null;
            }

            RunResponse runResponse = run(
                    RunRequest.builder()
                            .sandboxId(codeSandboxId)
                            .inputList(inputList)
                            .build()
            );
            if (runResponse == null) {
                listener.onSystemError();
                return null;
            }
            log.info("runResponse: {}", runResponse);
            if (!runResponse.getSuccess()) {
                if (RunResultEnum.TIME_LIMIT_EXCEEDED == RunResultEnum.fromValue(runResponse.getMessage())) {
                    listener.onTimeLimitExceeded(runResponse.getTimeUsed(), runResponse.getMemoryUsed());
                    return null;
                }
                if (RunResultEnum.MEMORY_LIMIT_EXCEEDED == RunResultEnum.fromValue(runResponse.getMessage())) {
                    listener.onMemoryLimitExceeded(runResponse.getTimeUsed(), runResponse.getMemoryUsed());
                    return null;
                }
                if (RunResultEnum.OUTPUT_LIMIT_EXCEEDED == RunResultEnum.fromValue(runResponse.getMessage())) {
                    listener.onOutputLimitExceeded(runResponse.getTimeUsed(), runResponse.getMemoryUsed());
                    return null;
                }
                if (RunResultEnum.RUNTIME_ERROR == RunResultEnum.fromValue(runResponse.getMessage())) {
                    listener.onRuntimeError(runResponse.getTimeUsed(), runResponse.getMemoryUsed());
                    return null;
                }
                listener.onSystemError();
                return null;
            }
            return ExecuteCodeResponse.builder()
                    .outputList(runResponse.getOutputList())
                    .status(runResponse.getMessage())
                    .timeUsed(runResponse.getTimeUsed())
                    .memoryUsed(runResponse.getMemoryUsed())
                    .build();

        } catch (Exception e) {
            listener.onSystemError();
            return null;
        } finally {
            if (codeSandboxId != null) {
                deleteFile(codeSandboxId);
            } else {
                log.info("codeSandboxId is null");
            }
        }

    }
}
