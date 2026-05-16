package com.fooddelivery.user.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项
 */
public class CartItem implements Serializable {
    private Long foodId;
    private String foodName;
    private String foodImage;
    private BigDecimal price;
    private Integer quantity;

    public CartItem() {
    }

    public CartItem(Long foodId, String foodName, String foodImage, BigDecimal price, Integer quantity) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodImage = foodImage;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 计算小计
     */
    public BigDecimal getSubtotal() {
        if (price != null && quantity != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
