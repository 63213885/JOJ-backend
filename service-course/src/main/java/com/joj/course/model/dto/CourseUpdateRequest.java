package com.joj.course.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 14:25
 */

@Data
public class CourseUpdateRequest {

    private Long id;

    private Integer sort;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer saleCount;

    /**
     * 0 下架，1 上架
     */
    private Integer status;
}
