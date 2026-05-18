package com.joj.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/17 17:08
 */

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    BANNED("封禁", 0),
    NORMAL("正常", 1);

    private final String text;

    private final Integer value;

    public static UserStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserStatusEnum val : values()) {
            if (val.value.equals(value)) {
                return val;
            }
        }
        return null;
    }

}
