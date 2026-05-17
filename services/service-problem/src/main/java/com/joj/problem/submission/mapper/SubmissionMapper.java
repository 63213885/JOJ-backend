package com.joj.problem.submission.mapper;

import com.joj.common.core.model.dto.UpdateSubmissionStatusRequest;
import com.joj.problem.submission.controller.dto.SubmissionQueryRequest;
import com.joj.common.core.model.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/28 11:58
 */

@Mapper
public interface SubmissionMapper {

    int insertSubmission(Submission submission);

    int deleteSubmissionById(@Param("id") Long id);

    int updateSubmissionById(Submission submission);

    int updateStatusIfCurrent(UpdateSubmissionStatusRequest updateSubmissionStatusRequest);

    Submission getSubmissionById(@Param("id") Long id);

    List<Submission> listSubmissions(SubmissionQueryRequest submissionQueryRequest);

}
