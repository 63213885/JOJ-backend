package com.joj.judge.service.impl;

import com.joj.api.ProblemFeginClient;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.dto.UpdateSubmissionStatusRequest;
import com.joj.common.core.model.entity.Problem;
import com.joj.common.core.model.entity.Submission;
import com.joj.common.core.model.enums.SubmissionStatusEnum;
import com.joj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.joj.judge.codeSandBox.model.ExecuteCodeResponse;
import com.joj.judge.codeSandBox.service.CodeSandBox;
import com.joj.judge.codeSandBox.service.CodeSandBoxFactory;
import com.joj.judge.listener.CodeSandboxExecuteListener;
import com.joj.judge.service.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/29 15:18
 */

@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private ProblemFeginClient problemFeginClient;

    @Value("${codesandbox.type}")
    private String type;

    @Override
    public void doJudge(Long submissionId) {
        Submission submission = problemFeginClient.getSubmission(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long problemId = submission.getProblemId();
        Problem problem = problemFeginClient.getProblemById(problemId);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目信息不存在");
        }

        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
//        codeSandBox = new CodeSandBoxProxy(codeSandBox);

        String code = submission.getCode();
        String language = submission.getLanguage();
        List<String> inputList = problem.getSamples().stream().map(Problem.Sample::getInput).collect(Collectors.toList());
        List<String> answerList = problem.getSamples().stream().map(Problem.Sample::getOutput).collect(Collectors.toList());
        Integer timeLimit = problem.getTimeLimit();
        Integer memoryLimit = problem.getMemoryLimit();

        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(
                ExecuteCodeRequest.builder()
                        .code(code)
                        .language(language)
                        .inputList(inputList)
                        .timeLimit(timeLimit)
                        .memoryLimit(memoryLimit)
                        .build(),
                new CodeSandboxExecuteListener() {

                    @Override
                    public Boolean onCompiling() {
                        return problemFeginClient.updateStatusIfCurrent(
                                UpdateSubmissionStatusRequest.builder()
                                        .submissionId(submission.getId())
                                        .currentStatus(SubmissionStatusEnum.PENDING.getValue())
                                        .targetStatus(SubmissionStatusEnum.COMPILING.getValue())
                                        .build()
                        );
                    }

                    @Override
                    public Boolean onRunning() {
                        return problemFeginClient.updateStatusIfCurrent(
                                UpdateSubmissionStatusRequest.builder()
                                        .submissionId(submission.getId())
                                        .currentStatus(SubmissionStatusEnum.COMPILING.getValue())
                                        .targetStatus(SubmissionStatusEnum.RUNNING.getValue())
                                        .build()
                        );
                    }

                    @Override
                    public void onCompileError() {
                        problemFeginClient.updateSubmission(
                                Submission.builder()
                                        .id(submissionId)
                                        .status(SubmissionStatusEnum.COMPILE_ERROR.getValue())
                                        .build()
                        );
                    }

                    @Override
                    public void onRuntimeError(Integer timeUsed, Integer memoryUsed) {
                        problemFeginClient.updateSubmission(
                                Submission.builder()
                                        .id(submissionId)
                                        .status(SubmissionStatusEnum.RUNTIME_ERROR.getValue())
                                        .timeUsed(timeUsed)
                                        .memoryUsed(memoryUsed)
                                        .build()
                        );
                    }

                    @Override
                    public void onMemoryLimitExceeded(Integer timeUsed, Integer memoryUsed) {
                        problemFeginClient.updateSubmission(
                                Submission.builder()
                                        .id(submissionId)
                                        .status(SubmissionStatusEnum.MEMORY_LIMIT_EXCEEDED.getValue())
                                        .timeUsed(timeUsed)
                                        .memoryUsed(memoryUsed)
                                        .build()
                        );
                    }

                    @Override
                    public void onTimeLimitExceeded(Integer timeUsed, Integer memoryUsed) {
                        problemFeginClient.updateSubmission(
                                Submission.builder()
                                        .id(submissionId)
                                        .status(SubmissionStatusEnum.TIME_LIMIT_EXCEEDED.getValue())
                                        .timeUsed(timeUsed)
                                        .memoryUsed(memoryUsed)
                                        .build()
                        );
                    }

                    @Override
                    public void onOutputLimitExceeded(Integer timeUsed, Integer memoryUsed) {
                        problemFeginClient.updateSubmission(
                                Submission.builder()
                                        .id(submissionId)
                                        .status(SubmissionStatusEnum.OUTPUT_LIMIT_EXCEEDED.getValue())
                                        .timeUsed(timeUsed)
                                        .memoryUsed(memoryUsed)
                                        .build()
                        );
                    }

                    @Override
                    public void onSystemError() {
                        problemFeginClient.updateSubmission(
                                Submission.builder()
                                        .id(submissionId)
                                        .status(SubmissionStatusEnum.SYSTEM_ERROR.getValue())
                                        .build()
                        );
                    }
                }
        );

        if (executeCodeResponse == null) {
            return;
        }

        List<String> outputList = executeCodeResponse.getOutputList();
        String status = executeCodeResponse.getStatus();
        Integer timeUsed = executeCodeResponse.getTimeUsed();
        Integer memoryUsed = executeCodeResponse.getMemoryUsed();

        log.info("Judge result: {}", executeCodeResponse);
        log.info("answer: {}", answerList);
        log.info("outputList: {}", outputList);

        if (timeUsed > timeLimit) {
            problemFeginClient.updateSubmission(
                    Submission.builder()
                            .id(submissionId)
                            .status(SubmissionStatusEnum.TIME_LIMIT_EXCEEDED.getValue())
                            .timeUsed(timeUsed)
                            .memoryUsed(memoryUsed)
                            .build()
            );
            return;
        }
        if (memoryUsed > memoryLimit * 1024 * 1024) {
            problemFeginClient.updateSubmission(
                    Submission.builder()
                            .id(submissionId)
                            .status(SubmissionStatusEnum.MEMORY_LIMIT_EXCEEDED.getValue())
                            .timeUsed(timeUsed)
                            .memoryUsed(memoryUsed)
                            .build()
            );
            return;
        }
        if (outputList == null ||  outputList.size() != answerList.size()) {
            problemFeginClient.updateSubmission(
                    Submission.builder()
                            .id(submissionId)
                            .status(SubmissionStatusEnum.WRONG_ANSWER.getValue())
                            .timeUsed(timeUsed)
                            .memoryUsed(memoryUsed)
                            .build()
            );
            return;
        }
        for (int i = 0; i < answerList.size(); i++) {
            if (!equalsIgnoreTrailingWhitespace(answerList.get(i), outputList.get(i))) {
                problemFeginClient.updateSubmission(
                        Submission.builder()
                                .id(submissionId)
                                .status(SubmissionStatusEnum.WRONG_ANSWER.getValue())
                                .timeUsed(timeUsed)
                                .memoryUsed(memoryUsed)
                                .build()
                );
                return;
            }
        }

        problemFeginClient.updateSubmission(
                Submission.builder()
                        .id(submissionId)
                        .status(SubmissionStatusEnum.ACCEPTED.getValue())
                        .timeUsed(timeUsed)
                        .memoryUsed(memoryUsed)
                        .build()
        );
    }

    private boolean equalsIgnoreTrailingWhitespace(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        a = a.replaceAll("\\s+$", "");
        b = b.replaceAll("\\s+$", "");
        log.info("equals??? {}   a = {}, b = {}", a.equals(b), a, b);
        return a.equals(b);
    }
}
