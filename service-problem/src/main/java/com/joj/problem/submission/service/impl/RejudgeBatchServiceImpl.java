package com.joj.problem.submission.service.impl;

import com.joj.common.core.model.enums.SubmissionStatusEnum;
import com.joj.common.core.model.mq.JudgeMessage;
import com.joj.problem.mq.JudgeMessageProducer;
import com.joj.problem.submission.mapper.SubmissionMapper;
import com.joj.problem.submission.service.RejudgeBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/19 19:39
 */

@Service
@RequiredArgsConstructor
public class RejudgeBatchServiceImpl implements RejudgeBatchService {

    private final SubmissionMapper submissionMapper;

    private final JudgeMessageProducer judgeMessageProducer;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void rejudgeBatch(List<Long> submissionIds) {
        if (submissionIds == null || submissionIds.isEmpty()) {
            return;
        }

        submissionMapper.resetBatchForRejudge(
                submissionIds,
                SubmissionStatusEnum.PENDING.getValue()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Long submissionId : submissionIds) {
                    judgeMessageProducer.sendJudgeMessage(
                            JudgeMessage.builder()
                                    .submissionId(submissionId)
                                    .build()
                    );
                }
            }
        });
    }
}
