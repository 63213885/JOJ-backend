package com.joj.codesandbox.sdk.enmus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/5 16:11
 */

@Getter
@AllArgsConstructor
public enum RunResultEnum {

    RUN_SUCCESS("运行成功", "Run Success"),
    RUNTIME_ERROR("运行时错误", "Runtime Error"),
    SYSTEM_ERROR("系统错误", "System Error"),

    MEMORY_LIMIT_EXCEEDED("超出内存限制", "Memory Limit Exceeded"),
    TIME_LIMIT_EXCEEDED("超出时间限制", "Time Limit Exceeded"),
    OUTPUT_LIMIT_EXCEEDED("输出超限", "Output Limit Exceeded");

    private final String text;

    private final String value;

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static RunResultEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RunResultEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }

}
