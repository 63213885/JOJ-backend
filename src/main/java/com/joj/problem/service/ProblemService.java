package com.joj.problem.service;

import com.joj.problem.controller.dto.CreateProblemRequest;
import com.joj.problem.controller.dto.ProblemVO;
import com.joj.problem.controller.dto.UpdateProblemRequest;

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

    ProblemVO getProblemById(Long id);

    List<ProblemVO> getProblemList(Integer limit, Integer offset);

}
