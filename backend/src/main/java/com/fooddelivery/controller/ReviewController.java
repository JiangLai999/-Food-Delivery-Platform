package com.fooddelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.CreateReviewDTO;
import com.fooddelivery.entity.Review;
import com.fooddelivery.service.ReviewService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 评价Controller
 */
@Slf4j
@Tag(name = "评价接口", description = "评价相关操作接口")
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "创建评价")
    @PostMapping("/create")
    public Result<String> createReview(@RequestHeader("Authorization") String token,
                                       @RequestBody @Valid CreateReviewDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        reviewService.createReview(userId, dto);
        return Result.success("评价成功");
    }

    @Operation(summary = "获取商家评价列表")
    @GetMapping("/merchant/{merchantId}")
    public Result<Page<Review>> getMerchantReviews(
            @Parameter(description = "商家ID") @PathVariable Long merchantId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Review> page = reviewService.getMerchantReviews(merchantId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "商家获取所有评价列表（包括待审核）")
    @GetMapping("/merchant/{merchantId}/all")
    public Result<Page<Review>> getMerchantAllReviews(
            @Parameter(description = "商家ID") @PathVariable Long merchantId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "审核状态: 0-待审核, 1-已通过, 2-已拒绝") @RequestParam(required = false) Integer status) {
        Page<Review> page = reviewService.getMerchantAllReviews(merchantId, pageNum, pageSize, status);
        return Result.success(page);
    }

    @Operation(summary = "获取用户评价列表")
    @GetMapping("/user")
    public Result<List<Review>> getUserReviews(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Review> reviews = reviewService.getUserReviews(userId);
        return Result.success(reviews);
    }

    @Operation(summary = "根据订单ID获取评价")
    @GetMapping("/order/{orderId}")
    public Result<Review> getReviewByOrderId(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        Review review = reviewService.getReviewByOrderId(orderId);
        return Result.success(review);
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/{reviewId}")
    public Result<String> deleteReview(@RequestHeader("Authorization") String token,
                                       @Parameter(description = "评价ID") @PathVariable Long reviewId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        reviewService.deleteReview(reviewId, userId);
        return Result.success("删除成功");
    }
}