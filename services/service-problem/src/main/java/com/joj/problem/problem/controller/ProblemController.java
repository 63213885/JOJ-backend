package com.joj.problem.problem.controller;

import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.problem.problem.controller.dto.CreateProblemRequest;
import com.joj.common.core.model.vo.ProblemVO;
import com.joj.problem.problem.controller.dto.UpdateProblemRequest;
import com.joj.problem.problem.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/22 21:38
 */

@Slf4j
@RestController
@RequestMapping("/problem")
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping
    public Result<Long> createProblem(@RequestBody CreateProblemRequest createProblemRequest) {
        Long id = problemService.createProblem(createProblemRequest);
        return Result.success(id);
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return Result.success();
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PutMapping("/{id}")
    public Result<Void> updateProblem(@PathVariable Long id, @RequestBody UpdateProblemRequest updateProblemRequest) {
        problemService.updateProblem(id, updateProblemRequest);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ProblemVO> getProblemById(@PathVariable Long id) {
        ProblemVO problem = problemService.getProblemVOById(id);
        return Result.success(problem);
    }

    @GetMapping("/list")
    public Result<List<ProblemVO>> getProblemList(@RequestParam(value = "limit", defaultValue = "50") Integer limit,
                                                  @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        List<ProblemVO> problems = problemService.getProblemList(limit, offset);
        return Result.success(problems);
    }

}
