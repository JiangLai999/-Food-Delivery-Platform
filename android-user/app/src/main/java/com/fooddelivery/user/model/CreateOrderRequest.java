package com.fooddelivery.user.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求模型
 */
public class CreateOrderRequest {
    private Long merchantId;
    private Long addressId;
    private List<OrderItemRequest> items;
    private String remark;
    private BigDecimal couponDiscount;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    /**
     * 订单项请求
     */
    public static class OrderItemRequest {
        private Long foodId;
        private Integer quantity;

        public OrderItemRequest() {
        }

        public OrderItemRequest(Long foodId, Integer quantity) {
            this.foodId = foodId;
            this.quantity = quantity;
        }

        public Long getFoodId() {
            return foodId;
        }

        public void setFoodId(Long foodId) {
            this.foodId = foodId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
