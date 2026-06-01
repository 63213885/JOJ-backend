package com.joj.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/28 23:16
 */

@Getter
@AllArgsConstructor
public enum MediaTranscodeStatusEnum {

    NO_NEED(0, "无需转码"),
    WAITING(1, "未转码"),
    TRANSCODING(2, "转码中"),
    SUCCESS(3, "已转码"),
    FAILED(4, "转码失败");

    private final Integer value;

    private final String text;

    public static MediaTranscodeStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MediaTranscodeStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

}
