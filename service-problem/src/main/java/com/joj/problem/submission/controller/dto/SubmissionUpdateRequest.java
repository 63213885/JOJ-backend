package com.joj.problem.submission.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/30 11:14
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionUpdateRequest {

    private Long userId;

    private Long problemId;

    private Long contestId;

    private String language;

    private String code;

    private String status;

    private Integer timeUsed;

    private Integer memoryUsed;

    private Integer score;

    private LocalDateTime submitTime;

}
