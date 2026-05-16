package com.fooddelivery.service;

import com.fooddelivery.dto.UserCouponDTO;

import java.util.List;

public interface CouponService {

    List<UserCouponDTO> getUserCoupons(Long userId);

    boolean useCoupon(Long userCouponId, Long orderId);
}
