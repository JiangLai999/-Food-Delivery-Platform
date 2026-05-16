package com.fooddelivery.user.model;

import java.math.BigDecimal;

/**
 * 骑手位置模型
 */
public class RiderLocation {
    private Long riderId;
    private String riderName;
    private String riderPhone;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer estimatedTime; // 预计送达时间（分钟）
    private String phase; // 配送阶段：going_to_merchant-赶往商家, delivering-配送中
    private Integer distanceToUser; // 距离用户距离（米）
    private String description; // 配送状态描述
    private String updateTime;

    public Long getRiderId() {
        return riderId;
    }

    public void setRiderId(Long riderId) {
        this.riderId = riderId;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getRiderPhone() {
        return riderPhone;
    }

    public void setRiderPhone(String riderPhone) {
        this.riderPhone = riderPhone;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public Integer getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public Integer getDistanceToUser() {
        return distanceToUser;
    }

    public void setDistanceToUser(Integer distanceToUser) {
        this.distanceToUser = distanceToUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
