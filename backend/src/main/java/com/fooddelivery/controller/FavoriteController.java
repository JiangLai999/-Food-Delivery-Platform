package com.fooddelivery.controller;

import com.fooddelivery.service.FavoriteService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.FavoriteMerchantVO;
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
@RequestMapping("/favorite")
@Tag(name = "收藏管理")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    @Operation(summary = "获取用户收藏列表")
    public Result<List<FavoriteMerchantVO>> getFavorites(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        List<FavoriteMerchantVO> favorites = favoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }

    @PostMapping("/addMerchant")
    @Operation(summary = "添加商家收藏")
    public Result<Void> addMerchantFavorite(@RequestParam Long merchantId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean success = favoriteService.addMerchantFavorite(userId, merchantId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("收藏失败");
        }
    }

    @PostMapping("/removeMerchant")
    @Operation(summary = "取消商家收藏")
    public Result<Void> removeMerchantFavorite(@RequestParam Long merchantId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean success = favoriteService.removeMerchantFavorite(userId, merchantId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("取消收藏失败");
        }
    }

    @GetMapping("/checkMerchant")
    @Operation(summary = "检查是否收藏商家")
    public Result<Boolean> checkMerchantFavorite(@RequestParam Long merchantId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.success(false);
        }
        boolean isFavorite = favoriteService.isMerchantFavorite(userId, merchantId);
        return Result.success(isFavorite);
    }

    @PostMapping("/addFood")
    @Operation(summary = "添加菜品收藏")
    public Result<Void> addFoodFavorite(@RequestParam Long foodId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean success = favoriteService.addFoodFavorite(userId, foodId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("收藏失败");
        }
    }

    @PostMapping("/removeFood")
    @Operation(summary = "取消菜品收藏")
    public Result<Void> removeFoodFavorite(@RequestParam Long foodId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean success = favoriteService.removeFoodFavorite(userId, foodId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("取消收藏失败");
        }
    }

    @GetMapping("/checkFood")
    @Operation(summary = "检查是否收藏菜品")
    public Result<Boolean> checkFoodFavorite(@RequestParam Long foodId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.success(false);
        }
        boolean isFavorite = favoriteService.isFoodFavorite(userId, foodId);
        return Result.success(isFavorite);
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
