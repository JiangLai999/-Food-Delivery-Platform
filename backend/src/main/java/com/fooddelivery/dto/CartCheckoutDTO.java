package com.fooddelivery.dto;

import com.fooddelivery.model.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车结算结果（简单示意，实际订单将由下单接口生成）
 */
@Data
public class CartCheckoutDTO {
    private List<CartItem> items;
    private BigDecimal totalAmount;
}
