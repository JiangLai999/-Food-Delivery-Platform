package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.dto.UserCouponDTO;
import com.fooddelivery.entity.Coupon;
import com.fooddelivery.entity.UserCoupon;
import com.fooddelivery.mapper.CouponMapper;
import com.fooddelivery.mapper.UserCouponMapper;
import com.fooddelivery.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Override
    public List<UserCouponDTO> getUserCoupons(Long userId) {
        List<UserCouponDTO> result = new ArrayList<>();

        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);

        for (UserCoupon uc : userCoupons) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon != null) {
                UserCouponDTO dto = new UserCouponDTO();
                dto.setId(uc.getId());
                dto.setUserId(uc.getUserId());
                dto.setCouponId(uc.getCouponId());
                dto.setStatus(uc.getStatus());
                dto.setUseTime(uc.getUseTime());
                dto.setCreateTime(uc.getCreateTime());

                dto.setTitle(coupon.getTitle());
                dto.setDescription(coupon.getDescription());
                dto.setAmount(coupon.getAmount());
                dto.setMinSpend(coupon.getMinSpend());
                dto.setStartTime(coupon.getStartTime());
                dto.setEndTime(coupon.getEndTime());
                dto.setMerchantId(coupon.getMerchantId());

                if (uc.getStatus() == 0) {
                    LocalDateTime now = LocalDateTime.now();
                    if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(now)) {
                        dto.setStatus(2);
                    }
                }

                result.add(dto);
            }
        }

        return result;
    }

    @Override
    public boolean useCoupon(Long userCouponId, Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || userCoupon.getStatus() != 0) {
            return false;
        }

        userCoupon.setStatus(1);
        userCoupon.setUseTime(LocalDateTime.now());
        userCoupon.setOrderId(orderId);

        int rows = userCouponMapper.updateById(userCoupon);
        return rows > 0;
    }
}
