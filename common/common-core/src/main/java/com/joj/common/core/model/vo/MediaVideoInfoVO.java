package com.joj.common.core.model.vo;

import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 17:45
 */

@Data
public class MediaVideoInfoVO {

    private Long fileId;

    private Integer transcodeStatus;

    private Integer encryptType;

    private String hlsPrefix;

    private String contentType;
}
