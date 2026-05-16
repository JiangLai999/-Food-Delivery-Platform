package com.fooddelivery.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO implements Serializable {

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;

    private String remark;

    private BigDecimal couponDiscount;  // 优惠券优惠金额

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "餐品ID不能为空")
        private Long foodId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
