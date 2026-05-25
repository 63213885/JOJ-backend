package com.joj.common.core.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:42
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseLesson {

    private Long id;

    private Long courseId;

    private String title;

    private String description;

    private Long videoFileId;

    /**
     * 视频时长，单位秒
     */
    private Integer duration;

    /**
     * 配套题目ID列表
     * <p>
     * 数据库中存 JSON：
     * [1001,1002,1003]
     */
    private List<Long> problemItems;

    private Integer sort;

    /**
     * 0 隐藏，1 显示
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}