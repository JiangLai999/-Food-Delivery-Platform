package com.fooddelivery.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCouponDTO implements Serializable {

    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private LocalDateTime useTime;
    private LocalDateTime createTime;

    private String title;
    private String description;
    private BigDecimal amount;
    private BigDecimal minSpend;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long merchantId;
}
