package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.CreateReviewDTO;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.ReviewImage;
import com.fooddelivery.mapper.OrderMapper;
import com.fooddelivery.mapper.ReviewImageMapper;
import com.fooddelivery.mapper.ReviewMapper;
import com.fooddelivery.service.ReviewService;
import com.fooddelivery.websocket.NativeWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价服务实现类
 */
@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public void createReview(Long userId, CreateReviewDTO dto) {
        // 检查订单是否存在且已完成
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 4) {
            throw new RuntimeException("订单未完成，无法评价");
        }

        // 检查是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, dto.getOrderId());
        Review existingReview = reviewMapper.selectOne(wrapper);
        if (existingReview != null) {
            throw new RuntimeException("订单已评价");
        }

        // 创建评价
        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setUserId(userId);
        review.setMerchantId(order.getMerchantId());
        review.setRating(dto.getRating());
        review.setTasteRating(dto.getTasteRating());
        review.setPortionRating(dto.getPortionRating());
        review.setContent(dto.getContent());
        review.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        review.setStatus(0); // 待审核

        reviewMapper.insert(review);

        // 保存评价图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (String imageUrl : dto.getImages()) {
                ReviewImage image = new ReviewImage();
                image.setReviewId(review.getId());
                image.setImageUrl(imageUrl);
                reviewImageMapper.insert(image);
            }
        }

        log.info("用户 {} 对订单 {} 进行了评价", userId, dto.getOrderId());
    }

    @Override
    public Page<Review> getMerchantReviews(Long merchantId, Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getMerchantId, merchantId)
                .eq(Review::getStatus, 1) // 只返回已通过审核的评价
                .orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Review> getUserReviews(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime);
        return reviewMapper.selectList(wrapper);
    }

    @Override
    public Review getReviewByOrderId(Long orderId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        return reviewMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void replyReview(Long reviewId, Long merchantId, String replyContent) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || !review.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("评价不存在");
        }

        review.setReplyContent(replyContent);
        review.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);

        log.info("商家 {} 回复了评价 {}", merchantId, reviewId);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || !review.getUserId().equals(userId)) {
            throw new RuntimeException("评价不存在");
        }

        reviewMapper.deleteById(reviewId);

        // 删除评价图片
        LambdaQueryWrapper<ReviewImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewImage::getReviewId, reviewId);
        reviewImageMapper.delete(wrapper);

        log.info("用户 {} 删除了评价 {}", userId, reviewId);
    }

    @Override
    public Page<Review> getPendingReviews(Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getStatus, 0) // 待审核
                .orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public void approveReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        review.setStatus(1); // 已通过
        reviewMapper.updateById(review);
        log.info("评价 {} 已审核通过", reviewId);

        // 通知商家有新评价通过审核
        if (review.getMerchantId() != null) {
            try {
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("type", "REVIEW_APPROVED");
                notificationData.put("reviewId", reviewId);
                notificationData.put("orderId", review.getOrderId());
                notificationData.put("content", review.getContent());
                notificationData.put("rating", review.getRating());
                notificationData.put("timestamp", review.getCreateTime());
                NativeWebSocketServer.sendChatMessageToMerchant(review.getMerchantId(), notificationData);
                log.info("已通知商家 {} 评价 {} 已通过审核", review.getMerchantId(), reviewId);
            } catch (Exception e) {
                log.error("通知商家失败", e);
            }
        }
    }

    @Override
    public void rejectReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        review.setStatus(2); // 已拒绝
        reviewMapper.updateById(review);
        log.info("评价 {} 已审核拒绝", reviewId);
    }

    @Override
    public Page<Review> getAllReviews(Integer pageNum, Integer pageSize, Integer status) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<Review> getMerchantAllReviews(Long merchantId, Integer pageNum, Integer pageSize, Integer status) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getMerchantId, merchantId);
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }
}
