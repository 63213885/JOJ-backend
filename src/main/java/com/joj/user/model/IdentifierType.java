package com.joj.user.model;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 16:01
 */

public enum IdentifierType {

    PHONE,
    EMAIL;

    public static IdentifierType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("identifier type required");
        }

        if (value.equals("phone")) {
            return PHONE;
        } else if (value.equals("email")) {
            return EMAIL;
        } else {
            throw new IllegalArgumentException("Unsupported identifier type: " + value);
        }
    }
}
