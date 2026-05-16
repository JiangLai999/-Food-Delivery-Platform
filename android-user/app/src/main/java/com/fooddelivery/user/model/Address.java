package com.fooddelivery.user.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收货地址模型
 */
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer isDefault; // 0-否，1-是
    private String tag; // 地址标签：家、公司、学校、其他
    private String createTime;
    private String updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
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

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        String result = "";
        if (province != null) {
            result += province;
        }
        if (city != null) {
            // 直辖市（北京市、上海市、天津市、重庆市）的省名和市名相同，避免重复
            if (!city.equals(province)) {
                result += city;
            }
        }
        if (district != null) {
            result += district;
        }
        if (detailAddress != null) {
            result += detailAddress;
        }
        return result;
    }

    /**
     * 便捷方法：是否为默认地址
     */
    public Boolean isDefault() {
        return isDefault != null && isDefault == 1;
    }

    /**
     * 便捷方法：设置默认地址
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault ? 1 : 0;
    }
}
