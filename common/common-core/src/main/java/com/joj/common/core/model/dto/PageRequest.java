package com.joj.common.core.model.dto;

import com.joj.common.core.model.enums.SortOrderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/17 15:45
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页号
     */
    @Min(value = 1, message = "当前页号必须大于或等于1")
    private int current = 1;

    /**
     * 页面大小
     */
    @Min(value = 1, message = "页面大小必须大于或等于1")
    @Max(value = 100, message = "页面大小必须小于或等于100")
    private int pageSize = 10;

    /**
     * 排序字段
     */
    @NotBlank(message = "排序字段不能为空")
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    @NotBlank
    private String sortOrder = SortOrderEnum.SORT_ORDER_ASC.getText();

}
