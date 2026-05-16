package com.fooddelivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.CreateReviewDTO;
import com.fooddelivery.entity.Review;

import java.util.List;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 创建评价
     */
    void createReview(Long userId, CreateReviewDTO dto);

    /**
     * 获取商家评价列表
     */
    Page<Review> getMerchantReviews(Long merchantId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户评价列表
     */
    List<Review> getUserReviews(Long userId);

    /**
     * 根据订单ID获取评价
     */
    Review getReviewByOrderId(Long orderId);

    /**
     * 商家回复评价
     */
    void replyReview(Long reviewId, Long merchantId, String replyContent);

    /**
     * 删除评价
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 获取待审核评价列表（管理端）
     */
    Page<Review> getPendingReviews(Integer pageNum, Integer pageSize);

    /**
     * 审核通过评价
     */
    void approveReview(Long reviewId);

    /**
     * 审核拒绝评价
     */
    void rejectReview(Long reviewId);

    /**
     * 获取所有评价列表（管理端）
     */
    Page<Review> getAllReviews(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 商家获取所有评价列表（包括待审核）
     */
    Page<Review> getMerchantAllReviews(Long merchantId, Integer pageNum, Integer pageSize, Integer status);
}
