package com.joj.common.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/28 23:46
 */

@Getter
@AllArgsConstructor
public enum MediaUploadTaskStatus {

    UPLOADING("上传中", 0),
    FINISHED("已完成", 1),
    ABORTED("已取消", 2),
    FAILED("失败", 3);

    private final String text;

    private final Integer value;

    public static MediaUploadTaskStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MediaUploadTaskStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

}
