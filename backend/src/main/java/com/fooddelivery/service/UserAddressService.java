package com.fooddelivery.service;

import com.fooddelivery.entity.UserAddress;

import java.util.List;

/**
 * 用户地址服务接口
 */
public interface UserAddressService {

    /**
     * 添加地址
     */
    void addAddress(UserAddress address);

    /**
     * 更新地址
     */
    void updateAddress(UserAddress address);

    /**
     * 删除地址
     */
    void deleteAddress(Long addressId, Long userId);

    /**
     * 获取用户所有地址
     */
    List<UserAddress> getUserAddresses(Long userId);

    /**
     * 获取默认地址
     */
    UserAddress getDefaultAddress(Long userId);

    /**
     * 设置默认地址
     */
    void setDefaultAddress(Long addressId, Long userId);

    /**
     * 获取地址详情
     */
    UserAddress getAddressById(Long addressId);
}
