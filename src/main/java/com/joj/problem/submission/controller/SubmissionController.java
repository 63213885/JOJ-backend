package com.joj.problem.submission.controller;

import com.joj.common.annotation.AuthCheck;
import com.joj.common.context.UserContext;
import com.joj.common.model.enums.SubmissionLanguageEnum;
import com.joj.common.model.enums.SubmissionStatusEnum;
import com.joj.common.model.enums.UserRoleEnum;
import com.joj.common.model.result.Result;
import com.joj.problem.submission.controller.dto.SubmissionQueryRequest;
import com.joj.problem.submission.controller.dto.SubmissionVO;
import com.joj.problem.submission.controller.dto.SubmitCodeRequest;
import com.joj.problem.submission.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
//        Boolean result = submissionService.updateSubmission(id, request);
//        return Result.success(result);
//    }

    @GetMapping("/{id}")
    public Result<SubmissionVO> getSubmission(@PathVariable Long id) {
        SubmissionVO submissionVO = submissionService.getSubmissionVO(id);
        return Result.success(submissionVO);
    }

    @GetMapping("/list")
    public Result<List<SubmissionVO>> listSubmissions(SubmissionQueryRequest request) {
        log.info("SubmissionQueryRequest =  {}", request);
        List<SubmissionVO> list = submissionService.listSubmissionVO(request);
        log.info("listSubmissions =  {}", list);
        return Result.success(list);
    }

    @GetMapping("/language/list")
    public Result<List<String>> listSupportedLanguages() {
        return Result.success(SubmissionLanguageEnum.getValues());
    }

    @GetMapping("/status")
    public Result<List<String>> listStatus() {
        return Result.success(SubmissionStatusEnum.getValues());
    }

}
