package com.joj.common.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/27 21:25
 */

@Getter
@AllArgsConstructor
public enum SubmissionLanguageEnum {

    CPP11("C++11"),
    CPP20("C++20"),
    JAVA("Java"),
    PYTHON3("Python3"),
    PYPY3("Pypy3"),
    GOLANG("Golang");

    private final String value;

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static SubmissionLanguageEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SubmissionLanguageEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }

}
