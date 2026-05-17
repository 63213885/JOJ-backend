package com.joj.problem.inner;

import com.joj.api.ProblemFeginClient;
import com.joj.common.core.model.dto.UpdateSubmissionStatusRequest;
import com.joj.common.core.model.entity.Problem;
import com.joj.common.core.model.entity.Submission;
import com.joj.common.core.model.enums.SubmissionStatusEnum;
import com.joj.problem.problem.service.ProblemService;
import com.joj.problem.submission.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/22 21:38
 */

@ApiIgnore
@Slf4j
@RestController
@RequestMapping("/inner")
public class ProblemInnerController implements ProblemFeginClient {

    @Resource
    private ProblemService problemService;

    @GetMapping("/problem/{id}")
    public Problem getProblemById(@PathVariable Long id) {
        Problem problem = problemService.getProblemById(id);
        return problem;
    }



    @Resource
    private SubmissionService submissionService;

    @GetMapping("/submission/{id}")
    public Submission getSubmission(@PathVariable Long id) {
        Submission submission = submissionService.getSubmission(id);
        return submission;
    }

    @PutMapping("/submission/update")
    public Boolean updateSubmission(@RequestBody Submission submission) {
        Boolean result = submissionService.updateSubmission(submission);
        return result;
    }

    @PutMapping("/submission/update/status/if-current")
    public Boolean updateStatusIfCurrent(@RequestBody UpdateSubmissionStatusRequest updateSubmissionStatusRequest) {
        Boolean result = submissionService.updateStatusIfCurrent(updateSubmissionStatusRequest);
        return result;
    }

}
