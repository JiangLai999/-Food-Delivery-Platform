package com.fooddelivery.user.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券/红包模型
 */
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String title;           // 优惠券标题
    private String description;     // 描述
    private BigDecimal amount;      // 优惠金额
    private BigDecimal minSpend;    // 最低消费金额
    private String type;            // 类型：CASH-现金券, DISCOUNT-折扣券
    private String validDate;       // 有效期
    private String status;          // 状态：UNUSED-未使用, USED-已使用, EXPIRED-已过期
    private Long merchantId;        // 适用商家ID（null表示通用）
    private String merchantName;    // 适用商家名称
    private String createTime;
    private String updateTime;
    private Long couponId;
    private Long userId;
    private String useTime;
    private String startTime;
    private String endTime;
    
    public Coupon() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getMinSpend() {
        return minSpend;
    }
    
    public void setMinSpend(BigDecimal minSpend) {
        this.minSpend = minSpend;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getValidDate() {
        return validDate;
    }
    
    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
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

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUseTime() {
        return useTime;
    }

    public void setUseTime(String useTime) {
        this.useTime = useTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    /**
     * 是否可用
     */
    public boolean isAvailable() {
        if (status == null) return false;
        if ("UNUSED".equals(status)) return true;
        if ("0".equals(status.toString())) return true;
        return false;
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        if ("UNUSED".equals(status)) return "未使用";
        if ("USED".equals(status)) return "已使用";
        if ("EXPIRED".equals(status)) return "已过期";
        if ("0".equals(status.toString())) return "未使用";
        if ("1".equals(status.toString())) return "已使用";
        if ("2".equals(status.toString())) return "已过期";
        return "未知";
    }
    
    /**
     * 获取类型文本
     */
    public String getTypeText() {
        if ("CASH".equals(type)) {
            return "现金券";
        } else if ("DISCOUNT".equals(type)) {
            return "折扣券";
        }
        return "优惠券";
    }
}
