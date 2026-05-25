package com.joj.common.core.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/24 00:41
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private Long id;

    private Long creatorId;

    private Integer sort;

    private String title;

    private String coverUrl;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer saleCount;

    /**
     * 0 下架，1 上架
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
