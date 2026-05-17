package com.joj.common.core.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
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
public class Problem implements Serializable {

    private Long id;

    private String title;
    private String content;

    private String inputDesc;
    private String outputDesc;

    private List<Sample> samples;

    private Integer timeLimit;
    private Integer memoryLimit;

    private Integer submitCount;
    private Integer acceptedCount;

    private List<String> tags;

    private List<String> source;

    private Long creatorId;

    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer isDelete;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sample {
        private String input;
        private String output;
    }

}
