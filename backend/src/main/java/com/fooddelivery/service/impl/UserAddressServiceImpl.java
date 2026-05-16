package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fooddelivery.entity.UserAddress;
import com.fooddelivery.mapper.UserAddressMapper;
import com.fooddelivery.service.GeocodingService;
import com.fooddelivery.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户地址服务实现类
 */
@Slf4j
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private GeocodingService geocodingService;

    @Override
    @Transactional
    public void addAddress(UserAddress address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelOtherDefaultAddress(address.getUserId());
        }

        if ((address.getLongitude() == null || address.getLatitude() == null)
                && address.getDetailAddress() != null) {
            String fullAddress = buildFullAddress(address);
            log.info("Attempting to geocode address: {}", fullAddress);
            BigDecimal[] coords = geocodingService.geocode(fullAddress);
            log.info("Geocoding result: {}", coords != null ? coords[0] + "," + coords[1] : "null");
            if (coords != null) {
                address.setLongitude(coords[0]);
                address.setLatitude(coords[1]);
                log.info("Auto-geocoded address {} to {},{}", fullAddress, coords[0], coords[1]);
            }
        }

        userAddressMapper.insert(address);
        log.info("用户 {} 添加地址成功", address.getUserId());
    }

    @Override
    @Transactional
    public void updateAddress(UserAddress address) {
        UserAddress existingAddress = userAddressMapper.selectById(address.getId());
        if (existingAddress == null || !existingAddress.getUserId().equals(address.getUserId())) {
            throw new RuntimeException("地址不存在或无权限");
        }

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelOtherDefaultAddress(address.getUserId());
        }

        if ((address.getLongitude() == null || address.getLatitude() == null)
                && address.getDetailAddress() != null) {
            String fullAddress = buildFullAddress(address);
            BigDecimal[] coords = geocodingService.geocode(fullAddress);
            if (coords != null) {
                address.setLongitude(coords[0]);
                address.setLatitude(coords[1]);
            }
        }

        userAddressMapper.updateById(address);
        log.info("用户 {} 更新地址成功", address.getUserId());
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        // 检查地址是否属于该用户
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限");
        }

        userAddressMapper.deleteById(addressId);
        log.info("用户 {} 删除地址 {} 成功", userId, addressId);
    }

    @Override
    public List<UserAddress> getUserAddresses(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime);
        return userAddressMapper.selectList(wrapper);
    }

    @Override
    public UserAddress getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("LIMIT 1");
        return userAddressMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long addressId, Long userId) {
        // 检查地址是否属于该用户
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限");
        }

        // 取消其他默认地址
        cancelOtherDefaultAddress(userId);

        // 设置为默认地址
        address.setIsDefault(1);
        userAddressMapper.updateById(address);

        log.info("用户 {} 设置默认地址 {} 成功", userId, addressId);
    }

    @Override
    public UserAddress getAddressById(Long addressId) {
        return userAddressMapper.selectById(addressId);
    }

    /**
     * 取消用户的其他默认地址
     */
    private void cancelOtherDefaultAddress(Long userId) {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0);
        userAddressMapper.update(null, wrapper);
    }

    private String buildFullAddress(UserAddress address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null) sb.append(address.getProvince());
        if (address.getCity() != null) sb.append(address.getCity());
        if (address.getDistrict() != null) sb.append(address.getDistrict());
        if (address.getDetailAddress() != null) sb.append(address.getDetailAddress());
        return sb.toString();
    }
}
