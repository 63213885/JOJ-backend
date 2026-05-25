package com.joj.course.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 14:27
 */

@Data
public class CourseVO {

    private Long id;

    private Long creatorId;

    private Integer sort;

    private String title;

    private String coverUrl;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer saleCount;

    private Integer status;

}
