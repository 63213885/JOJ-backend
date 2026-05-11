package com.joj.problem.submission.service.impl;

import com.joj.common.exception.BusinessException;
import com.joj.common.exception.ErrorCode;
import com.joj.common.model.enums.SubmissionStatusEnum;
import com.joj.judge.service.JudgeService;
import com.joj.problem.problem.service.ProblemService;
import com.joj.problem.submission.controller.dto.SubmissionQueryRequest;
import com.joj.problem.submission.controller.dto.SubmissionUpdateRequest;
import com.joj.problem.submission.controller.dto.SubmissionVO;
import com.joj.problem.submission.controller.dto.SubmitCodeRequest;
import com.joj.problem.submission.mapper.SubmissionMapper;
import com.joj.problem.submission.model.Submission;
import com.joj.problem.submission.service.SubmissionService;
import com.joj.user.auth.model.Entity.User;
import com.joj.user.auth.service.UserService;
import com.joj.user.profile.controller.dto.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/28 11:58
 */

@Slf4j
@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Resource
    private SubmissionMapper submissionMapper;
    @Resource
    private UserService userService;
    @Resource
    private ProblemService problemService;
    @Autowired
    private JudgeService judgeService;

    @Transactional
    @Override
    public Long submitCode(Long userId, SubmitCodeRequest submitCodeRequest) {
        Submission submission = new Submission();
        BeanUtils.copyProperties(submitCodeRequest, submission);
        submission.setUserId(userId);
        submission.setStatus(SubmissionStatusEnum.PENDING.getValue());
        submission.setTimeUsed(0);
        submission.setMemoryUsed(0);
        submission.setScore(0);
        submissionMapper.insertSubmission(submission);
        log.info("submission id = {}", submission.getId());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                judgeService.doJudge(submission.getId());
            }
        });

        return submission.getId();
    }

    @Transactional
    @Override
    public Boolean deleteSubmission(Long id) {
        int delete = submissionMapper.deleteSubmissionById(id);
        return delete > 0;
    }

    @Transactional
    @Override
    public Submission getSubmission(Long id) {
        Submission submission = submissionMapper.getSubmissionById(id);
        return submission;
    }

    @Transactional
    @Override
    public SubmissionVO toSubmissionVO(Submission submission) {
        SubmissionVO submissionVO = new SubmissionVO();
        BeanUtils.copyProperties(submission, submissionVO);
        submissionVO.setUser(userService.getUserVOById(submission.getUserId()));
        submissionVO.setProblem(problemService.getProblemVOById(submission.getProblemId()));
        return submissionVO;
    }

    @Transactional
    @Override
    public SubmissionVO getSubmissionVO(Long id) {
        Submission submission = getSubmission(id);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
        }
        return toSubmissionVO(submission);
    }

    @Transactional
    @Override
    public Boolean updateSubmission(Submission submission) {
        int update = submissionMapper.updateSubmissionById(submission);
        return update > 0;
    }

    @Transactional
    @Override
    public List<SubmissionVO> listSubmissionVO(SubmissionQueryRequest submissionQueryRequest) {
        List<Submission> listSubmission = submissionMapper.listSubmissions(submissionQueryRequest);
        log.info("listSubmission =  {}", listSubmission);
        return listSubmission.stream().map(submission -> {
            return toSubmissionVO(submission);
        }).collect(Collectors.toList());
    }

}
