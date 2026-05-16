package com.fooddelivery.user.model;

import com.google.gson.annotations.SerializedName;

public class DeliveryTask {
    public Long orderId;
    public Long riderId;
    public String riderName;
    public String riderPhone;
    public Integer status;
    public String routeData;
    public Integer currentPosition;
    public Integer estimatedTime;

    @SerializedName("pickupLatitude")
    public Double pickupLatitude;

    @SerializedName("pickupLongitude")
    public Double pickupLongitude;

    @SerializedName("deliveryLatitude")
    public Double deliveryLatitude;

    @SerializedName("deliveryLongitude")
    public Double deliveryLongitude;

    public Integer getStatus() {
        return status;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public Integer getEstimatedTime() {
        return estimatedTime;
    }
    
    public String getRouteData() {
        return routeData;
    }
    
    public Integer getCurrentPosition() {
        return currentPosition;
    }
}
