package com.fooddelivery.user.model;

import java.io.Serializable;
import java.util.List;

public class Restaurant implements Serializable {
    private String id;
    private String name;
    private double rating;
    private String deliveryTime;
    private double deliveryFee;
    private String image;
    private List<FoodItem> menu;
    private String announcement;
    private String hours;

    public Restaurant() {
    }

    public Restaurant(String id, String name, double rating, String deliveryTime, 
                      double deliveryFee, String image, List<FoodItem> menu) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.deliveryFee = deliveryFee;
        this.image = image;
        this.menu = menu;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public List<FoodItem> getMenu() { return menu; }
    public void setMenu(List<FoodItem> menu) { this.menu = menu; }

    public String getAnnouncement() { return announcement; }
    public void setAnnouncement(String announcement) { this.announcement = announcement; }

    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }
}
