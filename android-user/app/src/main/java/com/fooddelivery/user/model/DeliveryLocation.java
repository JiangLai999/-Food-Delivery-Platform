package com.fooddelivery.user.model;

import java.math.BigDecimal;

public class DeliveryLocation {
    public Long orderId;
    public Long riderId;
    public String riderName;
    public String riderPhone;
    public BigDecimal longitude;
    public BigDecimal latitude;
    public Integer status;
    public Integer estimatedTime;
    public String phase;
    public Integer distanceToUser;
    public String description;
    public String updateTime;
    public Integer currentPosition; // 当前配送进度位置
}
