package com.joj.common.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/27 21:31
 */

@Getter
@AllArgsConstructor
public enum SubmissionStatusEnum {

    PENDING("待办", "Pending"),
    RUNNING("运行中", "Running"),

    ACCEPTED("答案正确", "Accepted"),
    WRONG_ANSWER("答案错误", "Wrong Answer"),
    COMPILE_ERROR("编译错误", "Compile Error"),
    PRESENTATION_ERROR("格式错误", "Presentation Error"),
    RUNTIME_ERROR("运行时错误", "Runtime Error"),

    MEMORY_LIMIT_EXCEEDED("超出内存限制", "Memory Limit Exceeded"),
    TIME_LIMIT_EXCEEDED("超出时间限制", "Time Limit Exceeded"),

    OUTPUT_LIMIT_EXCEEDED("输出超限", "Output Limit Exceeded"),

    SYSTEM_ERROR("系统错误", "System Error");

    private final String text;

    private final String value;

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static SubmissionStatusEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SubmissionStatusEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }

}
