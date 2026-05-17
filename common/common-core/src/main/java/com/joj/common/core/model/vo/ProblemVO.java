package com.joj.common.core.model.vo;

import com.joj.common.core.model.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/23 23:03
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemVO implements Serializable {

    private Long id;

    private String title;
    private String content;

    private String inputDesc;
    private String outputDesc;

    private List<Problem.Sample> samples;

    private Integer timeLimit;
    private Integer memoryLimit;

    private Integer submitCount;
    private Integer acceptedCount;

    private List<String> tags;

    private List<String> source;

    private Long creatorId;

    private Integer status;

}
