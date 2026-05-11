package com.joj.problem.problem.service;

import com.joj.problem.problem.controller.dto.CreateProblemRequest;
import com.joj.problem.problem.controller.dto.ProblemVO;
import com.joj.problem.problem.controller.dto.UpdateProblemRequest;
import com.joj.problem.problem.model.Problem;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/23 22:44
 */

public interface ProblemService {

    Long createProblem(CreateProblemRequest createProblemRequest);

    void deleteProblem(Long id);

    void updateProblem(Long id, UpdateProblemRequest updateProblemRequest);

    Problem getProblemById(Long id);

    ProblemVO getProblemVOById(Long id);

    List<ProblemVO> getProblemList(Integer limit, Integer offset);

}
