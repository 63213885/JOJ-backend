package com.joj.course.model.dto;

import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/25 23:42
 */

@Data
public class VideoPlayUrlVO {

    private String playUrl;

    private Integer expireSeconds;
}
