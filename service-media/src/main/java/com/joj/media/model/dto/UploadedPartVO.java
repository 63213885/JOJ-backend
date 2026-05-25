package com.joj.media.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:53
 */

@Data
@Builder
public class UploadedPartVO {

    private Integer partNumber;

    private String etag;

    private Long partSize;
}
