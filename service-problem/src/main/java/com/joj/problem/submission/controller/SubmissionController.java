package com.joj.problem.submission.controller;

import com.joj.common.core.context.UserContext;
import com.joj.common.core.model.dto.PageRequest;
import com.joj.common.core.model.dto.PageResponse;
import com.joj.common.core.model.enums.SubmissionLanguageEnum;
import com.joj.common.core.model.enums.SubmissionStatusEnum;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.problem.submission.controller.dto.SubmissionQueryRequest;
import com.joj.common.core.model.vo.SubmissionVO;
import com.joj.problem.submission.controller.dto.SubmitCodeRequest;
import com.joj.problem.submission.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/28 11:57
 */

@Slf4j
@RestController
@RequestMapping("/submission")
public class SubmissionController {

    @Resource
    private SubmissionService submissionService;

    @AuthCheck
    @PostMapping
    public Result<Long> submitCode(@RequestBody SubmitCodeRequest submitCodeRequest) {
        Long userId = UserContext.get().getId();
        Long submissionId = submissionService.submitCode(userId, submitCodeRequest);
        return Result.success(submissionId);
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSubmission(@PathVariable Long id) {
        Boolean result = submissionService.deleteSubmission(id);
        return Result.success(result);
    }

//    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
//    @PutMapping("/{id}")
//    public Result<Boolean> updateSubmission(@PathVariable Long id, @RequestBody SubmissionUpdateRequest request) {
//        Submission submission = new Submission();
//        submission.setId(id);
//        BeanUtils.copyProperties(request, submission);
//        Boolean result = submissionService.updateSubmission(submission);
//        return Result.success(result);
//    }

    @GetMapping("/{id}")
    public Result<SubmissionVO> getSubmission(@PathVariable Long id) {
        SubmissionVO submissionVO = submissionService.getSubmissionVO(id);
        return Result.success(submissionVO);
    }

    @GetMapping("/list")
    public Result<PageResponse<SubmissionVO>> listSubmissions(@Valid PageRequest pageRequest, SubmissionQueryRequest request) {
        log.info("SubmissionQueryRequest =  {}", request);
        Long current = pageRequest.getCurrent();
        Long pageSize = pageRequest.getPageSize();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();

        Long offset = (current - 1) * pageSize;
        Long limit = pageSize;
        List<SubmissionVO> list = submissionService.listSubmissionVO(request, offset, limit);
        log.info("listSubmissions =  {}", list);
        return Result.success(
                PageResponse.<SubmissionVO>builder()
                        .records(list)
                        .total(null)
                        .build()
        );
    }

    @GetMapping("/language/list")
    public Result<List<String>> listSupportedLanguages() {
        return Result.success(SubmissionLanguageEnum.getValues());
    }

    @GetMapping("/status")
    public Result<List<String>> listStatus() {
        return Result.success(SubmissionStatusEnum.getValues());
    }

//    第一阶段：
//    提交记录先入库，事务提交后发送 MQ。
//    再加一个定时任务扫描 Waiting 超时的提交，重新投递。
//
//    第二阶段：
//    引入 judge_message_outbox 表，所有判题消息都走本地消息表。
//
//    第三阶段：
//    RabbitMQ publisher confirm + outbox 状态更新 + 失败重试。
//
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/{id}/rejudge")
    public Result<Boolean> rejudgeSubmission(@PathVariable Long id) {
        return Result.success(submissionService.rejudgeSubmission(id));
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/list/rejudge")
    public Result<Boolean> rejudgeListSubmissions(SubmissionQueryRequest submissionQueryRequest) {
        return Result.success(submissionService.rejudgeListSubmissions(submissionQueryRequest));
    }

}
