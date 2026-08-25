package com.joj.problem.submission.service;

import com.joj.common.core.model.dto.UpdateSubmissionStatusRequest;
import com.joj.common.core.model.enums.SubmissionStatusEnum;
import com.joj.problem.submission.controller.dto.SubmissionQueryRequest;
import com.joj.common.core.model.vo.SubmissionVO;
import com.joj.problem.submission.controller.dto.SubmitCodeRequest;
import com.joj.common.core.model.entity.Submission;


import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/28 11:57
 */

public interface SubmissionService {

    Long submitCode(Long userId, SubmitCodeRequest submitCodeRequest);

    Boolean deleteSubmission(Long id);

    Boolean updateSubmission(Submission submission);

    Boolean updateStatusIfCurrent(UpdateSubmissionStatusRequest updateSubmissionStatusRequest);

    Submission getSubmission(Long id);

    SubmissionVO toSubmissionVO(Submission submission);

    SubmissionVO getSubmissionVO(Long id);

//    List<Submission> listSubmissions(SubmissionQueryRequest submissionQueryRequest, Integer offset, Integer limit);

    List<SubmissionVO> listSubmissionVO(SubmissionQueryRequest submissionQueryRequest, Long offset, Long limit);

    Boolean rejudgeSubmission(Long submissionId);

    Boolean rejudgeListSubmissions(SubmissionQueryRequest submissionQueryRequest);

}
