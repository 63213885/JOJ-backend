package com.joj.judge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 18:56
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompileRequest {

    private String language;

    private String code;

    private Integer timeLimit;

}
