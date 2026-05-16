package com.fooddelivery.user.model;

import java.math.BigDecimal;
import java.util.Date;

public class FavoriteMerchant {
    private Long id;
    private Long merchantId;
    private String merchantName;
    private String logo;
    private BigDecimal rating;
    private Integer salesVolume;
    private BigDecimal deliveryFee;
    private BigDecimal minAmount;
    private Integer averageDeliveryTime;
    private String favoriteTime;
    private String favoriteType;
    private Long foodId;
    private String foodName;
    private String foodImage;
    private BigDecimal foodPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getSalesVolume() { return salesVolume; }
    public void setSalesVolume(Integer salesVolume) { this.salesVolume = salesVolume; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public Integer getAverageDeliveryTime() { return averageDeliveryTime; }
    public void setAverageDeliveryTime(Integer averageDeliveryTime) { this.averageDeliveryTime = averageDeliveryTime; }

    public String getFavoriteTime() { return favoriteTime; }
    public void setFavoriteTime(String favoriteTime) { this.favoriteTime = favoriteTime; }

    public String getFavoriteType() { return favoriteType; }
    public void setFavoriteType(String favoriteType) { this.favoriteType = favoriteType; }

    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodImage() { return foodImage; }
    public void setFoodImage(String foodImage) { this.foodImage = foodImage; }

    public BigDecimal getFoodPrice() { return foodPrice; }
    public void setFoodPrice(BigDecimal foodPrice) { this.foodPrice = foodPrice; }
}
