package com.joj.problem.problem.service.impl;

import com.joj.common.core.model.entity.User;
import com.joj.common.core.model.enums.ProblemStatusEnum;
import com.joj.common.core.context.UserContext;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.problem.problem.controller.dto.CreateProblemRequest;
import com.joj.common.core.model.vo.ProblemVO;
import com.joj.problem.problem.controller.dto.UpdateProblemRequest;
import com.joj.problem.problem.mapper.ProblemMapper;
import com.joj.common.core.model.entity.Problem;
import com.joj.problem.problem.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/23 22:44
 */

@Service
@Slf4j
public class ProblemServiceImpl implements ProblemService {

    @Resource
    private ProblemMapper problemMapper;

    @Transactional
    @Override
    public Long createProblem(CreateProblemRequest createProblemRequest) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(createProblemRequest, problem);

        problem.setSubmitCount(0);
        problem.setAcceptedCount(0);
        problem.setCreatorId(UserContext.get().getId());
        problem.setIsDelete(0);

        problemMapper.insert(problem);
        return problem.getId();
    }

    @Transactional
    @Override
    public void deleteProblem(Long id) {
        problemMapper.deleteById(id);
    }

    @Transactional
    @Override
    public void updateProblem(Long id, UpdateProblemRequest updateProblemRequest) {
        Problem problem = new Problem();
        problem.setId(id);
        BeanUtils.copyProperties(updateProblemRequest, problem);
        problemMapper.updateById(problem);
    }

    @Transactional
    public Problem getProblemById(Long id) {
        Problem problem = problemMapper.findById(id);
        return problem;
    }

    @Transactional
    @Override
    public ProblemVO getProblemVOById(Long id) {
        Problem problem = getProblemById(id);

        if (ProblemStatusEnum.fromValue(problem.getStatus()) == ProblemStatusEnum.HIDE
                && (UserContext.get() == null || UserRoleEnum.fromValue(UserContext.get().getRole()) != UserRoleEnum.ADMIN)) {
            return null;
        }
        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problem, problemVO);
        return problemVO;
    }

    @Transactional
    @Override
    public List<ProblemVO> getProblemList(Integer limit, Integer offset) {
        User user = UserContext.get();
        Boolean isAdmin = user != null && UserRoleEnum.fromValue(user.getRole()) == UserRoleEnum.ADMIN;
        List<Problem> problems = problemMapper.selectProblemPageByStatus(limit, offset, isAdmin);
        return problems.stream().map(problem -> {
            ProblemVO problemVO = new ProblemVO();
            BeanUtils.copyProperties(problem, problemVO);
            return problemVO;
        }).collect(Collectors.toList());
    }
}
