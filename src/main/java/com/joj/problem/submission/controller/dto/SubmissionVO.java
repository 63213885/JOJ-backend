package com.joj.problem.submission.controller.dto;

import com.joj.problem.problem.controller.dto.ProblemVO;
import com.joj.user.profile.controller.dto.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/28 11:35
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionVO {

    private Long id;

    private UserVO user;

    private ProblemVO problem;

    private String language;

    private String code;

    private String status;

    private Integer timeUsed;

    private Integer memoryUsed;

    private Integer score;

    private LocalDateTime submitTime;

}
