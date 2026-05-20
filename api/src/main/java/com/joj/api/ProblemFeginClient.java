package com.joj.api;

import com.joj.common.core.model.dto.UpdateSubmissionStatusRequest;
import com.joj.common.core.model.entity.Problem;
import com.joj.common.core.model.entity.Submission;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/14 21:37
 */

@FeignClient(name = "service-problem", path = "/api/inner")
public interface ProblemFeginClient {

    @GetMapping("/problem/{id}")
    Problem getProblemById(@PathVariable Long id);

    @GetMapping("/submission/{id}")
    Submission getSubmission(@PathVariable Long id);

    @PutMapping("/submission/update")
    Boolean updateSubmission(@RequestBody Submission submission);

    @PutMapping("/submission/update/status/if-current")
    Boolean updateStatusIfCurrent(@RequestBody UpdateSubmissionStatusRequest updateSubmissionStatusRequest);

}
