package com.fooddelivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.CreateOrderDTO;
import com.fooddelivery.entity.Order;
import com.fooddelivery.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(Long userId, CreateOrderDTO dto);

    /**
     * 支付订单
     */
    void payOrder(Long orderId, Long userId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId, String reason);

    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 获取用户订单列表
     */
    Page<OrderVO> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 商家接单
     */
    void acceptOrder(Long orderId, Long merchantId);

    /**
     * 商家拒绝接单
     */
    void rejectOrder(Long orderId, Long merchantId, String reason);

    /**
     * 获取商家订单列表
     */
    Page<OrderVO> getMerchantOrders(Long merchantId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 确认订单完成
     */
    void confirmComplete(Long orderId, Long userId);

    /**
     * 构建订单VO
     */
    OrderVO buildOrderVO(Order order);
}
