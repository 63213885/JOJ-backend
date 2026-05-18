package com.joj.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/17 15:48
 */

@Getter
@AllArgsConstructor
public enum SortOrderEnum {

    SORT_ORDER_ASC("升序", "ascend"),
    SORT_ORDER_DESC("降序", "descend");

    private final String text;

    private final String value;

    public static SortOrderEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SortOrderEnum val : values()) {
            if (val.value.equals(value)) {
                return val;
            }
        }
        return null;
    }

}
