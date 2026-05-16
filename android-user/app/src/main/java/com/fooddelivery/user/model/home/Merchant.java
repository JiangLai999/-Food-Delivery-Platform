package com.fooddelivery.user.model.home;

public class Merchant {
    private final String name;
    private final double rating;
    private final String imageUrl;
    private final String deliveryInfo;

    public Merchant(String name, double rating, String imageUrl, String deliveryInfo) {
        this.name = name;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.deliveryInfo = deliveryInfo;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDeliveryInfo() {
        return deliveryInfo;
    }
}
