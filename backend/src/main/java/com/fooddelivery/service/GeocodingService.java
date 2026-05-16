package com.fooddelivery.service;

import com.fooddelivery.entity.UserAddress;
import java.math.BigDecimal;

public interface GeocodingService {
    /**
     * 根据地址文本获取坐标
     * @param address 地址文本
     * @return 经纬度数组 [longitude, latitude]，如果失败返回null
     */
    BigDecimal[] geocode(String address);
}
