package com.fooddelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.Review;
import com.fooddelivery.service.ReviewService;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端评价审核Controller
 */
@Slf4j
@Tag(name = "评价审核管理", description = "管理端评价审核接口")
@RestController
@RequestMapping("/admin/review")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/pending")
    @Operation(summary = "获取待审核评价列表")
    public Result<Page<Review>> getPendingReviews(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Review> page = reviewService.getPendingReviews(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有评价列表")
    public Result<Page<Review>> getAllReviews(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "审核状态: 0-待审核, 1-已通过, 2-已拒绝") @RequestParam(required = false) Integer status) {
        Page<Review> page = reviewService.getAllReviews(pageNum, pageSize, status);
        return Result.success(page);
    }

    @PostMapping("/approve/{reviewId}")
    @Operation(summary = "审核通过评价")
    public Result<Void> approveReview(
            @Parameter(description = "评价ID") @PathVariable Long reviewId) {
        reviewService.approveReview(reviewId);
        return Result.success();
    }

    @PostMapping("/reject/{reviewId}")
    @Operation(summary = "审核拒绝评价")
    public Result<Void> rejectReview(
            @Parameter(description = "评价ID") @PathVariable Long reviewId) {
        reviewService.rejectReview(reviewId);
        return Result.success();
    }
}
