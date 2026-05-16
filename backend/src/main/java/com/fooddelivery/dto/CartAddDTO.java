package com.fooddelivery.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 购物车添加项 DTO
 */
@Data
public class CartAddDTO {
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotNull(message = "餐品ID不能为空")
    private Long foodId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;
}
