package com.joj.problem.submission.service;

import com.joj.problem.submission.controller.dto.SubmissionQueryRequest;
import com.joj.problem.submission.controller.dto.SubmissionVO;
import com.joj.problem.submission.controller.dto.SubmitCodeRequest;
import com.joj.problem.submission.model.Submission;

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

    Submission getSubmission(Long id);

    SubmissionVO toSubmissionVO(Submission submission);

    SubmissionVO getSubmissionVO(Long id);

    List<SubmissionVO> listSubmissionVO(SubmissionQueryRequest submissionQueryRequest);

}
