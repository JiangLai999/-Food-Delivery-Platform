package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.CreateOrderDTO;
import com.fooddelivery.entity.*;
import com.fooddelivery.entity.Review;
import com.fooddelivery.mapper.*;
import com.fooddelivery.service.*;
import com.fooddelivery.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private RiderMapper riderMapper;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Order createOrder(Long userId, CreateOrderDTO dto) {
        // 获取商家信息
        Merchant merchant = merchantMapper.selectById(dto.getMerchantId());
        if (merchant == null || merchant.getStatus() != 1) {
            throw new RuntimeException("商家不存在或未营业");
        }

        // 获取收货地址
        UserAddress address = userAddressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }

        // 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            FoodItem food = foodItemMapper.selectById(item.getFoodId());
            if (food == null || food.getStatus() != 1) {
                throw new RuntimeException("餐品不存在或已下架");
            }
            if (food.getStock() < item.getQuantity()) {
                throw new RuntimeException("餐品库存不足");
            }
            totalAmount = totalAmount.add(food.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // 检查起送价
        if (totalAmount.compareTo(merchant.getMinOrderAmount()) < 0) {
            throw new RuntimeException("未达到起送价");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setMerchantId(dto.getMerchantId());
        order.setTotalAmount(totalAmount.add(merchant.getDeliveryFee()).add(BigDecimal.valueOf(2.00))); // 商品金额+配送费+包装费
        order.setDeliveryFee(merchant.getDeliveryFee());
        order.setPackFee(BigDecimal.valueOf(2.00)); // 默认打包费2元

        // 优惠券优惠
        BigDecimal couponDiscount = dto.getCouponDiscount() != null ? dto.getCouponDiscount() : BigDecimal.ZERO;
        order.setCouponDiscount(couponDiscount);

        // 计算最终金额：总金额 - 优惠券优惠
        order.setFinalAmount(order.getTotalAmount().subtract(couponDiscount));
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() +
                                 address.getDistrict() + address.getDetailAddress());
        order.setReceiverLongitude(address.getLongitude());
        order.setReceiverLatitude(address.getLatitude());
        order.setRemark(dto.getRemark());
        order.setStatus(0); // 待支付
        order.setPayStatus(0); // 未支付

        orderMapper.insert(order);

        // 创建订单明细
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            FoodItem food = foodItemMapper.selectById(item.getFoodId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setFoodId(item.getFoodId());
            orderItem.setFoodName(food.getFoodName());
            orderItem.setFoodImage(food.getImage());
            orderItem.setPrice(food.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(food.getPrice().multiply(new BigDecimal(item.getQuantity())));

            orderItemMapper.insert(orderItem);

            // 扣减库存
            food.setStock(food.getStock() - item.getQuantity());
            foodItemMapper.updateById(food);
        }

        log.info("创建订单成功，订单号: {}", order.getOrderNo());
        return order;
    }

    @Override
    @Transactional
    public void payOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确");
        }

        // 更新订单状态
        order.setStatus(1); // 待接单
        order.setPayStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单 {} 支付成功", order.getOrderNo());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("订单已接单，无法取消");
        }

        // 更新订单状态
        order.setStatus(5); // 已取消
        order.setCancelReason(reason);
        orderMapper.updateById(order);

        // 恢复库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        for (OrderItem item : items) {
            FoodItem food = foodItemMapper.selectById(item.getFoodId());
            if (food != null) {
                food.setStock(food.getStock() + item.getQuantity());
                foodItemMapper.updateById(food);
            }
        }

        log.info("订单 {} 已取消", order.getOrderNo());
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        return buildOrderVO(order);
    }

    @Override
    public Page<OrderVO> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        Page<OrderVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public void acceptOrder(Long orderId, Long merchantId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确");
        }

        // 更新订单状态
        order.setStatus(2); // 已接单
        order.setAcceptTime(LocalDateTime.now());
        
        // 分配骑手（默认骑手ID=1）
        Long fixedRiderId = 1L;
        order.setRiderId(fixedRiderId);
        orderMapper.updateById(order);
        
        // 更新骑手状态
        Rider rider = riderMapper.selectById(fixedRiderId);
        if (rider != null) {
            rider.setStatus(2); // 配送中
            riderMapper.updateById(rider);
        }

        log.info("商家接单成功，订单号: {}, 已分配骑手: {}", order.getOrderNo(), fixedRiderId);
        
        // 立即创建配送任务（同步）
        try {
            log.info("开始为订单{}创建配送任务", orderId);
            deliveryService.createDeliveryTask(orderId, fixedRiderId);
            deliveryService.startDelivery(orderId);
            
            // 增加销量
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);
            for (OrderItem item : items) {
                foodItemService.increaseSalesVolume(item.getFoodId(), item.getQuantity());
            }
            
            log.info("订单{}配送任务创建成功", orderId);
        } catch (Exception e) {
            log.error("创建配送任务失败，订单ID: {}, 错误: {}", orderId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId, Long merchantId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确");
        }

        // 更新订单状态
        order.setStatus(5); // 已取消
        order.setCancelReason("商家拒单: " + reason);
        orderMapper.updateById(order);

        log.info("商家拒单，订单号: {}", order.getOrderNo());
    }

    @Override
    public Page<OrderVO> getMerchantOrders(Long merchantId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        Page<OrderVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public void confirmComplete(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 3 && order.getStatus() != 4) {
            throw new RuntimeException("订单状态不正确");
        }

        // 更新订单状态
        order.setStatus(4); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单 {} 已确认完成", order.getOrderNo());
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return timestamp + random;
    }

    /**
     * 构建订单VO
     */
    @Override
    public OrderVO buildOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        // 设置用户信息
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUserPhone(user.getPhone());
        }

        // 设置商家信息
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        if (merchant != null) {
            vo.setMerchantName(merchant.getMerchantName());
            vo.setMerchantLogo(merchant.getLogo());
            vo.setMerchantLongitude(merchant.getLongitude());
            vo.setMerchantLatitude(merchant.getLatitude());
            vo.setMerchant(merchant);
        }

        // 设置骑手信息
        if (order.getRiderId() != null) {
            Rider rider = riderMapper.selectById(order.getRiderId());
            if (rider != null) {
                vo.setRiderName(rider.getRiderName());
            }
        }

        // 设置订单状态文本
        vo.setStatusText(getStatusText(order.getStatus()));

        // 设置订单明细
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        vo.setItems(orderItemMapper.selectList(wrapper));

        // 检查是否已评价
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getOrderId, order.getId());
        Review review = reviewMapper.selectOne(reviewWrapper);
        vo.setHasReviewed(review != null);

        return vo;
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "待接单";
            case 2: return "已接单";
            case 3: return "配送中";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知状态";
        }
    }
}
