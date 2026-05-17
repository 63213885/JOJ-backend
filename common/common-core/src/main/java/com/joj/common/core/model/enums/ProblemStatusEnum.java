package com.joj.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/26 15:21
 */

@Getter
@AllArgsConstructor
public enum ProblemStatusEnum {

    HIDE("隐藏", 0),
    SHOW("显示", 1);

    private final String text;

    private final Integer value;

    public static ProblemStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ProblemStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

}
