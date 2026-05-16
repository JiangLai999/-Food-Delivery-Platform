package com.fooddelivery.user.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 商家模型
 */
public class Merchant implements Serializable {
    private Long id;

    @SerializedName(value = "name", alternate = {"merchantName"})
    private String name;

    private String phone;
    private Long categoryId;
    private String categoryName;
    private String logo;
    private String banner;

    @SerializedName(value = "address", alternate = {"detailAddress"})
    private String address;

    private BigDecimal longitude;
    private BigDecimal latitude;
    private String description;
    private String businessHours;
    private BigDecimal deliveryFee;

    @SerializedName(value = "minAmount", alternate = {"minOrderAmount"})
    private BigDecimal minAmount;

    private BigDecimal packFee;

    @SerializedName(value = "rating", alternate = {"avgRating"})
    private Float rating;

    private Integer salesVolume;
    private Integer status; // 0-停业，1-营业中
    private String notice;
    private String createTime;
    private String updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getPackFee() {
        return packFee;
    }

    public void setPackFee(BigDecimal packFee) {
        this.packFee = packFee;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Integer getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(Integer salesVolume) {
        this.salesVolume = salesVolume;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取营业状态文本
     */
    public String getStatusText() {
        return status != null && status == 1 ? "营业中" : "休息中";
    }

    // ==================== 补充缺失的方法 (2026-02-20 修复) ====================
    
    /**
     * 获取商家图片（用于列表显示）
     */
    public String getImage() {
        return logo != null ? logo : (banner != null ? banner : "");
    }
    
    /**
     * 获取月销量
     */
    public int getMonthlySales() {
        return salesVolume != null ? salesVolume : 0;
    }
    
    /**
     * 获取起送价（转换为 double）
     */
    public double getMinOrder() {
        return minAmount != null ? minAmount.doubleValue() : 0.0;
    }
    
    /**
     * 获取平均配送时间（分钟）- 模拟值
     */
    public int getAverageDeliveryTime() {
        return 30;
    }
    
    /**
     * 获取距离（公里）- 模拟值
     */
    public double getDistance() {
        return 2.5;
    }
    
    /**
     * 获取标签列表 - 模拟值
     */
    public String getTags() {
        return "好评如潮，快速配送，满减优惠";
    }
    
    /**
     * 获取分类（别名方法）
     */
    public String getCategory() {
        return categoryName != null ? categoryName : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Merchant merchant = (Merchant) o;
        return Objects.equals(id, merchant.id) &&
                Objects.equals(name, merchant.name) &&
                Objects.equals(phone, merchant.phone) &&
                Objects.equals(categoryId, merchant.categoryId) &&
                Objects.equals(categoryName, merchant.categoryName) &&
                Objects.equals(logo, merchant.logo) &&
                Objects.equals(banner, merchant.banner) &&
                Objects.equals(address, merchant.address) &&
                Objects.equals(longitude, merchant.longitude) &&
                Objects.equals(latitude, merchant.latitude) &&
                Objects.equals(description, merchant.description) &&
                Objects.equals(businessHours, merchant.businessHours) &&
                Objects.equals(deliveryFee, merchant.deliveryFee) &&
                Objects.equals(minAmount, merchant.minAmount) &&
                Objects.equals(packFee, merchant.packFee) &&
                Objects.equals(rating, merchant.rating) &&
                Objects.equals(salesVolume, merchant.salesVolume) &&
                Objects.equals(status, merchant.status) &&
                Objects.equals(notice, merchant.notice) &&
                Objects.equals(createTime, merchant.createTime) &&
                Objects.equals(updateTime, merchant.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phone, categoryId, categoryName, logo, banner, address, longitude, latitude, description, businessHours, deliveryFee, minAmount, packFee, rating, salesVolume, status, notice, createTime, updateTime);
    }
}
