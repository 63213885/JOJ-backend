package com.joj.common.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/16 20:14
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeMessage implements Serializable {

    private Long submissionId;

    /**
     * 普通提交为 null
     * 比赛提交不为 null
     */
    private Long contestId;
}
