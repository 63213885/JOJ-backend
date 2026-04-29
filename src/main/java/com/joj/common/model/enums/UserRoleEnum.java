package com.joj.common.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/25 22:42
 */

@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    USER("普通用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    public static UserRoleEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }

}
