package com.fooddelivery.controller;

import com.fooddelivery.dto.UserCouponDTO;
import com.fooddelivery.service.CouponService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/coupon")
@Tag(name = "优惠券管理")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/user")
    @Operation(summary = "获取用户优惠券")
    public Result<List<UserCouponDTO>> getUserCoupons(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<UserCouponDTO> coupons = couponService.getUserCoupons(userId);
        return Result.success(coupons);
    }

    @PostMapping("/use")
    @Operation(summary = "使用优惠券")
    public Result<Void> useCoupon(@RequestParam Long userCouponId, @RequestParam Long orderId) {
        boolean success = couponService.useCoupon(userCouponId, orderId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("使用优惠券失败");
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("解析token失败", e);
        }
        return null;
    }
}
