package com.fooddelivery.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteMerchantVO {

    private Long id;

    private Long merchantId;

    private String merchantName;

    private String logo;

    private BigDecimal rating;

    private Integer salesVolume;

    private BigDecimal deliveryFee;

    private BigDecimal minAmount;

    private Integer averageDeliveryTime;

    private LocalDateTime favoriteTime;

    private String favoriteType;

    private Long foodId;

    private String foodName;

    private String foodImage;

    private BigDecimal foodPrice;
}
