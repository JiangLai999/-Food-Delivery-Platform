package com.fooddelivery.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 简易购物车条目（前端购物车的后端表示，当前实现为内存缓存，不持久化到数据库）
 */
@Data
public class CartItem {
    private Long foodId;
    private Long merchantId;
    private String foodName;
    private String foodImage;
    private BigDecimal price;
    private Integer quantity;
}
