package com.joj.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 15:19
 */

@Getter
@AllArgsConstructor
public enum MediaAccessTypeEnum {

    PRIVATE(0, "私密"),
    PUBLIC(1, "公开");

    private final Integer value;

    private final String text;

    public static MediaAccessTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MediaAccessTypeEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

}
