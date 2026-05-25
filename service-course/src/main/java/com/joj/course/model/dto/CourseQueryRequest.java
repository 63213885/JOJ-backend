package com.joj.course.model.dto;

import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 14:26
 */

@Data
public class CourseQueryRequest {

    /**
     * 0 下架，1 上架，null 查询全部
     */
    private Integer status;
}
