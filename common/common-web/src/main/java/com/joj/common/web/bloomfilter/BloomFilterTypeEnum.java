package com.joj.common.web.bloomfilter;

import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/6/25 00:04
 */

@Getter
public enum BloomFilterTypeEnum {

    USER_ID("joj:bloom:user:id", 1_000_000L, 0.001),

    PROBLEM_ID("joj:bloom:problem:id", 1_000_000L, 0.001),

    COURSE_ID("joj:bloom:course:id", 100_000L, 0.001),

    LESSON_ID("joj:bloom:lesson:id", 500_000L, 0.001);

    private final String key;

    private final long expectedInsertions;

    private final double falseProbability;

    BloomFilterTypeEnum(String key, long expectedInsertions, double falseProbability) {
        this.key = key;
        this.expectedInsertions = expectedInsertions;
        this.falseProbability = falseProbability;
    }
}
