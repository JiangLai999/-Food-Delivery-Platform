package com.fooddelivery.controller;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商家营业设置DTO
 */
@Data
public class MerchantSettingsDTO {
    private String businessHours;
    private BigDecimal deliveryFee;
    private BigDecimal minOrderAmount;
}
