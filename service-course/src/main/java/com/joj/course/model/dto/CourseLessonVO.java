package com.joj.course.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 14:27
 */

@Data
public class CourseLessonVO {

    private Long id;

    private Long courseId;

    private String title;

    private String description;

    private Long videoFileId;

    private Integer duration;

    private List<Long> problemItems;

    private Integer sort;

    private Integer status;

}
