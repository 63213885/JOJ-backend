package com.joj.problem.problem.controller.dto;

import com.joj.problem.problem.model.Problem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/26 13:43
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProblemRequest {

    private String title;
    private String content;

    private String inputDesc;
    private String outputDesc;

    private List<Problem.Sample> samples;

    private Integer timeLimit;
    private Integer memoryLimit;

    private List<String> tags;

    private List<String> source;

    private Integer status;

}
