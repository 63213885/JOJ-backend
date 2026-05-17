package com.joj.problem.submission.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/28 12:06
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCodeRequest {

    private Long problemId;

    private Long contestId;

    private String language;

    private String code;

}
