package com.fooddelivery.user.model;

public class DeliveryCreateDTO {
    private Long orderId;
    private Long riderId;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getRiderId() { return riderId; }
    public void setRiderId(Long riderId) { this.riderId = riderId; }
}
