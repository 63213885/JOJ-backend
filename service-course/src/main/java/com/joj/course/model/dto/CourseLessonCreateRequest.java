package com.joj.course.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 14:26
 */

@Data
public class CourseLessonCreateRequest {

    private Long courseId;

    private String title;

    private String description;

    private Integer sort;

    /**
     * 0 隐藏，1 显示
     */
    private Integer status;
}
