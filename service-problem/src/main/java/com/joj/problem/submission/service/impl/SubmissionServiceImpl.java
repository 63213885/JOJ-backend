package com.joj.problem.submission.service.impl;

import com.joj.api.UserFeignClient;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.dto.UpdateSubmissionStatusRequest;
import com.joj.common.core.model.enums.SubmissionStatusEnum;
import com.joj.common.core.model.mq.JudgeMessage;
import com.joj.problem.mq.JudgeMessageProducer;
import com.joj.problem.problem.service.ProblemService;
import com.joj.problem.submission.controller.dto.SubmissionQueryRequest;
import com.joj.common.core.model.vo.SubmissionVO;
import com.joj.problem.submission.controller.dto.SubmitCodeRequest;
import com.joj.problem.submission.mapper.SubmissionMapper;
import com.joj.common.core.model.entity.Submission;
import com.joj.problem.submission.service.RejudgeBatchService;
import com.joj.problem.submission.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
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
    private UserFeignClient userFeignClient;
    @Resource
    private ProblemService problemService;
    @Resource
    private JudgeMessageProducer judgeMessageProducer;
    @Resource
    private RejudgeBatchService rejudgeBatchService;

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

//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//            @Override
//            public void afterCommit() {
//                judgeService.doJudge(submission.getId());
//            }
//        });

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                judgeMessageProducer.sendJudgeMessage(
                        JudgeMessage.builder()
                                .submissionId(submission.getId())
                                .build()
                );
            }
        });
        // 这里用 afterCommit() 的原因是：数据库事务提交成功后再发 MQ。

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
    public Boolean updateSubmission(Submission submission) {
        int update = submissionMapper.updateSubmissionById(submission);
        return update > 0;
    }

    @Transactional
    @Override
    public Boolean updateStatusIfCurrent(UpdateSubmissionStatusRequest updateSubmissionStatusRequest) {
        int update = submissionMapper.updateStatusIfCurrent(updateSubmissionStatusRequest);
        return update > 0;
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
        submissionVO.setUser(userFeignClient.getUserVOById(submission.getUserId()));
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

//    @Transactional
//    @Override
//    public List<Submission> listSubmissions(SubmissionQueryRequest submissionQueryRequest, Integer offset, Integer limit) {
//        return submissionMapper.listSubmissions(submissionQueryRequest, offset, limit);
//    }

    @Transactional
    @Override
    public List<SubmissionVO> listSubmissionVO(SubmissionQueryRequest submissionQueryRequest, Long offset, Long limit) {
        List<Submission> listSubmission = submissionMapper.listSubmissions(submissionQueryRequest, offset, limit);
        log.info("listSubmission =  {}", listSubmission);
        return listSubmission.stream().map(submission -> {
            return toSubmissionVO(submission);
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean rejudgeSubmission(Long submissionId) {
        if (submissionMapper.getSubmissionById(submissionId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
        }
        submissionMapper.updateSubmissionById(
                Submission.builder()
                        .id(submissionId)
                        .status(SubmissionStatusEnum.PENDING.getValue())
                        .build()
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                judgeMessageProducer.sendJudgeMessage(
                        JudgeMessage.builder()
                                .submissionId(submissionId)
                                .build()
                );
            }
        });
        return true;
    }

    @Override
    public Boolean rejudgeListSubmissions(SubmissionQueryRequest submissionQueryRequest) {
        Long lastId = 0L;
        Long limit = 100L;

        while (true) {
            List<Long> ids = submissionMapper.listIds(submissionQueryRequest, lastId, limit);
            if (ids == null || ids.isEmpty()) {
                break;
            }

            rejudgeBatchService.rejudgeBatch(ids);
            lastId = ids.get(ids.size() - 1);
        }
        return true;
    }

}
