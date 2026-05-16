package com.fooddelivery.user.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单模型
 */
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Long riderId;
    private String riderName;
    private String riderPhone;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal packFee;
    private BigDecimal finalAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal receiverLongitude;
    private BigDecimal receiverLatitude;
    private String remark;
    private Integer status; // 0-待支付，1-待接单，2-已接单，3-配送中，4-已完成，5-已取消
    private Integer payStatus; // 0-未支付，1-已支付
    private String payTime;
    private String acceptTime;
    private String deliveryTime;
    private String completeTime;
    private String cancelReason;
    private String createTime;
    private String updateTime;
    
    // 商家名称（API直接返回）
    private String merchantName;
    
    // 商家Logo（API直接返回）
    private String merchantLogo;
    
    // 是否已评价
    private Boolean hasReviewed;
    
    // 关联信息
    private Merchant merchant;
    private List<OrderItem> items;

    public void setMerchantName(String name) {
        if (this.merchant == null) {
            this.merchant = new Merchant();
        }
        this.merchant.setName(name);
        this.merchantName = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

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

    public String getMerchantName() {
        if (merchantName != null && !merchantName.isEmpty()) {
            return merchantName;
        }
        if (merchant != null) {
            return merchant.getName();
        }
        return null;
    }
    
    public String getMerchantNameDirect() {
        return merchantName;
    }
    
    public void setMerchantNameDirect(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantLogo() {
        return merchantLogo;
    }

    public void setMerchantLogo(String logo) {
        this.merchantLogo = logo;
    }

    public Boolean getHasReviewed() {
        return hasReviewed;
    }

    public void setHasReviewed(Boolean hasReviewed) {
        this.hasReviewed = hasReviewed;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getPackFee() {
        return packFee;
    }

    public void setPackFee(BigDecimal packFee) {
        this.packFee = packFee;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public BigDecimal getReceiverLongitude() {
        return receiverLongitude;
    }

    public void setReceiverLongitude(BigDecimal receiverLongitude) {
        this.receiverLongitude = receiverLongitude;
    }

    public BigDecimal getReceiverLatitude() {
        return receiverLatitude;
    }

    public void setReceiverLatitude(BigDecimal receiverLatitude) {
        this.receiverLatitude = receiverLatitude;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(String acceptTime) {
        this.acceptTime = acceptTime;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * 获取订单状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "待接单";
            case 2: return "已接单";
            case 3: return "配送中";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}
