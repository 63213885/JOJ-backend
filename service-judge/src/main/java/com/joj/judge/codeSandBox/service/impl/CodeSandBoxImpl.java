package com.joj.judge.codeSandBox.service.impl;

import com.joj.api.ProblemFeginClient;
import com.joj.codesandbox.sdk.client.CodeSandBoxClient;
import com.joj.codesandbox.sdk.dto.CompileRequest;
import com.joj.codesandbox.sdk.dto.CompileResponse;
import com.joj.codesandbox.sdk.dto.RunRequest;
import com.joj.codesandbox.sdk.dto.RunResponse;
import com.joj.codesandbox.sdk.enmus.RunResultEnum;
import com.joj.common.core.model.dto.UpdateSubmissionStatusRequest;
import com.joj.common.core.model.entity.Submission;
import com.joj.common.core.model.enums.SubmissionStatusEnum;
import com.joj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.joj.judge.codeSandBox.model.ExecuteCodeResponse;
import com.joj.judge.codeSandBox.service.CodeSandBox;
import com.joj.judge.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/20 20:18
 */

@Slf4j
@Service
public class CodeSandBoxImpl implements CodeSandBox {

    @Resource
    private CodeSandBoxClient codeSandBoxClient;
    @Resource
    private ProblemFeginClient problemFeginClient;

    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "jzz";


    private CompileResponse compile(CompileRequest request) {
        return codeSandBoxClient.compile(request, AUTH_REQUEST_SECRET);
    }

    private RunResponse run(RunRequest request) {
        return codeSandBoxClient.run(request, AUTH_REQUEST_SECRET);
    }

    private void deleteFile(String sandboxId) {
        codeSandBoxClient.deleteFile(sandboxId, AUTH_REQUEST_SECRET);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        Long submissionId = executeCodeRequest.getSubmissionId();
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        Integer timeLimit = executeCodeRequest.getTimeLimit();
        Integer memoryLimit = executeCodeRequest.getMemoryLimit();
        log.info("execute code, language: {}, timeLimit: {}, memoryLimit: {}", language, timeLimit, memoryLimit);

        String codeSandboxId = null;
        try {
            Boolean ok = problemFeginClient.updateStatusIfCurrent(
                    UpdateSubmissionStatusRequest.builder()
                            .submissionId(submissionId)
                            .currentStatus(SubmissionStatusEnum.PENDING.getValue())
                            .targetStatus(SubmissionStatusEnum.COMPILING.getValue())
                            .build()
            );
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
                problemFeginClient.updateSubmission(
                        Submission.builder()
                                .id(submissionId)
                                .status(SubmissionStatusEnum.SYSTEM_ERROR.getValue())
                                .build()
                );
                return null;
            }
            if (!compileResponse.getSuccess()) {
                log.info("message: {}", compileResponse.getMessage());
                problemFeginClient.updateSubmission(
                        Submission.builder()
                                .id(submissionId)
                                .status(SubmissionStatusEnum.COMPILE_ERROR.getValue())
                                .build()
                );
                return null;
            }
            if (compileResponse.getSandboxId() == null) {
                problemFeginClient.updateSubmission(
                        Submission.builder()
                                .id(submissionId)
                                .status(SubmissionStatusEnum.SYSTEM_ERROR.getValue())
                                .build()
                );
                return null;
            }
            codeSandboxId = compileResponse.getSandboxId();

            ok = problemFeginClient.updateStatusIfCurrent(
                    UpdateSubmissionStatusRequest.builder()
                            .submissionId(submissionId)
                            .currentStatus(SubmissionStatusEnum.COMPILING.getValue())
                            .targetStatus(SubmissionStatusEnum.RUNNING.getValue())
                            .build()
            );
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
                problemFeginClient.updateSubmission(
                        Submission.builder()
                                .id(submissionId)
                                .status(SubmissionStatusEnum.SYSTEM_ERROR.getValue())
                                .build()
                );
                return null;
            }
            log.info("runResponse: {}", runResponse);
            if (!runResponse.getSuccess()) {
                if (RunResultEnum.TIME_LIMIT_EXCEEDED == RunResultEnum.fromValue(runResponse.getMessage())) {
                    problemFeginClient.updateSubmission(
                            Submission.builder()
                                    .id(submissionId)
                                    .status(SubmissionStatusEnum.TIME_LIMIT_EXCEEDED.getValue())
                                    .timeUsed(runResponse.getTimeUsed())
                                    .memoryUsed(runResponse.getMemoryUsed())
                                    .build()
                    );
                    return null;
                }
                if (RunResultEnum.MEMORY_LIMIT_EXCEEDED == RunResultEnum.fromValue(runResponse.getMessage())) {
                    problemFeginClient.updateSubmission(
                            Submission.builder()
                                    .id(submissionId)
                                    .status(SubmissionStatusEnum.MEMORY_LIMIT_EXCEEDED.getValue())
                                    .timeUsed(runResponse.getTimeUsed())
                                    .memoryUsed(runResponse.getMemoryUsed())
                                    .build()
                    );
                    return null;
                }
                if (RunResultEnum.OUTPUT_LIMIT_EXCEEDED == RunResultEnum.fromValue(runResponse.getMessage())) {
                    problemFeginClient.updateSubmission(
                            Submission.builder()
                                    .id(submissionId)
                                    .status(SubmissionStatusEnum.OUTPUT_LIMIT_EXCEEDED.getValue())
                                    .timeUsed(runResponse.getTimeUsed())
                                    .memoryUsed(runResponse.getMemoryUsed())
                                    .build()
                    );
                    return null;
                }
                if (RunResultEnum.RUNTIME_ERROR == RunResultEnum.fromValue(runResponse.getMessage())) {
                    problemFeginClient.updateSubmission(
                            Submission.builder()
                                    .id(submissionId)
                                    .status(SubmissionStatusEnum.RUNTIME_ERROR.getValue())
                                    .timeUsed(runResponse.getTimeUsed())
                                    .memoryUsed(runResponse.getMemoryUsed())
                                    .build()
                    );
                    return null;
                }
                problemFeginClient.updateSubmission(
                        Submission.builder()
                                .id(submissionId)
                                .status(SubmissionStatusEnum.SYSTEM_ERROR.getValue())
                                .build()
                );
                return null;
            }
            return ExecuteCodeResponse.builder()
                    .outputList(runResponse.getOutputList())
                    .status(runResponse.getMessage())
                    .timeUsed(runResponse.getTimeUsed())
                    .memoryUsed(runResponse.getMemoryUsed())
                    .build();

        } catch (Exception e) {
            problemFeginClient.updateSubmission(
                    Submission.builder()
                            .id(submissionId)
                            .status(SubmissionStatusEnum.SYSTEM_ERROR.getValue())
                            .build()
            );
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
