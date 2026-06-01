package com.joj.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/28 23:18
 */

@Getter
@AllArgsConstructor
public enum MediaEncryptTypeEnum {

    NONE(0, "无加密"),
    HLS_AES_128_KEY_OBFUSCATED(1, "HLS AES-128 + Key混淆");

    private final Integer value;

    private final String text;

    public static MediaEncryptTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MediaEncryptTypeEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

}
