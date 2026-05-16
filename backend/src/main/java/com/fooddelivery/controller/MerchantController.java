package com.fooddelivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.MerchantLoginDTO;
import com.fooddelivery.dto.MerchantRegisterDTO;
import com.fooddelivery.entity.*;
import com.fooddelivery.mapper.FoodItemMapper;
import com.fooddelivery.mapper.MerchantMapper;
import com.fooddelivery.mapper.OrderMapper;
import com.fooddelivery.mapper.OrderItemMapper;
import com.fooddelivery.mapper.ReviewMapper;
import com.fooddelivery.mapper.UserMapper;
import com.fooddelivery.service.*;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.OrderVO;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import com.fooddelivery.dto.MerchantResetPasswordDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Tag(name = "商家接口", description = "商家相关操作接口")
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private FoodCategoryService foodCategoryService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private MerchantCategoryService merchantCategoryService;

    @Operation(summary = "商家注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid MerchantRegisterDTO dto) {
        merchantService.register(dto);
        return Result.success("注册成功，请等待审核");
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestParam @NotBlank String phone) {
        merchantService.sendVerifyCode(phone);
        return Result.success("验证码发送成功");
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody MerchantResetPasswordDTO dto) {
        merchantService.resetPassword(dto.getPhone(), dto.getCode(), dto.getNewPassword());
        return Result.success("密码重置成功");
    }

    @Operation(summary = "商家登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid MerchantLoginDTO dto) {
        String token = merchantService.login(dto);
        Merchant merchant = merchantService.getMerchantById(jwtUtil.getUserIdFromToken("Bearer " + token));

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("merchant", merchant);

        return Result.success("登录成功", data);
    }

    @Operation(summary = "获取商家信息")
    @GetMapping("/info")
    public Result<Merchant> getMerchantInfo(@RequestHeader("Authorization") String token) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        Merchant merchant = merchantService.getMerchantById(merchantId);
        return Result.success(merchant);
    }

    @Operation(summary = "更新商家信息")
    @PutMapping("/update")
    public Result<String> updateMerchant(@RequestHeader("Authorization") String token,
                                         @RequestBody Merchant merchant) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        merchant.setId(merchantId);
        merchantService.updateMerchant(merchant);
        return Result.success("更新成功");
    }

    @Operation(summary = "搜索商家")
    @GetMapping("/search")
    public Result<Page<Merchant>> searchMerchants(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Merchant> page = merchantService.searchMerchants(keyword, categoryId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取热门商家")
    @GetMapping("/hot")
    public Result<List<Merchant>> getHotMerchants(
            @Parameter(description = "返回数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        List<Merchant> merchants = merchantService.getHotMerchants(limit);
        return Result.success(merchants);
    }

    @Operation(summary = "获取商家详情")
    @GetMapping("/{merchantId}")
    public Result<Merchant> getMerchantById(
            @Parameter(description = "商家ID") @PathVariable Long merchantId) {
        Merchant merchant = merchantService.getMerchantById(merchantId);
        return Result.success(merchant);
    }

    @Operation(summary = "更新营业状态")
    @PutMapping("/status")
    public Result<String> updateBusinessStatus(@RequestHeader("Authorization") String token,
                                               @Parameter(description = "状态: 0-休息, 1-营业") @RequestParam Integer status) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        merchantService.updateBusinessStatus(merchantId, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "获取商家订单列表")
    @GetMapping("/orders")
    public Result<Page<OrderVO>> getMerchantOrders(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        Page<OrderVO> orders = orderService.getMerchantOrders(merchantId, status, pageNum, pageSize);
        return Result.success(orders);
    }

    @Operation(summary = "商家回复评价")
    @PostMapping("/reply-review/{reviewId}")
    public Result<String> replyReview(@RequestHeader("Authorization") String token,
                                      @Parameter(description = "评价ID") @PathVariable Long reviewId,
                                      @Parameter(description = "回复内容") @RequestParam String replyContent) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        reviewService.replyReview(reviewId, merchantId, replyContent);
        return Result.success("回复成功");
    }

    @Operation(summary = "商家接单")
    @PostMapping("/order/accept/{orderId}")
    public Result<String> acceptOrder(@RequestHeader("Authorization") String token,
                                      @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        log.info("接单请求 - 订单ID: {}, 商家ID: {}", orderId, merchantId);
        orderService.acceptOrder(orderId, merchantId);
        return Result.success("接单成功");
    }

    @Operation(summary = "商家拒单")
    @PostMapping("/order/reject/{orderId}")
    public Result<String> rejectOrder(@RequestHeader("Authorization") String token,
                                      @Parameter(description = "订单ID") @PathVariable Long orderId,
                                      @Parameter(description = "拒单原因") @RequestParam String reason) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        orderService.rejectOrder(orderId, merchantId, reason);
        return Result.success("拒单成功");
    }

    @Operation(summary = "测试接口：手动创建配送任务")
    @GetMapping("/order/accept/{orderId}/test")
    public Result<String> acceptOrderTest(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        log.info("测试接单 - 订单ID: {}", orderId);
        Long merchantId = 1L;
        orderService.acceptOrder(orderId, merchantId);
        return Result.success("测试接单成功");
    }
    
    @Operation(summary = "测试接口：为已接单订单手动创建配送任务")
    @PostMapping("/order/{orderId}/create-delivery")
    public Result<String> createDeliveryTask(@PathVariable Long orderId) {
        log.info("手动创建配送任务 - 订单ID: {}", orderId);
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        Long merchantId = order.getMerchantId();
        orderService.acceptOrder(orderId, merchantId);
        return Result.success("配送任务创建成功");
    }
    
    @Operation(summary = "测试接口：直接创建配送任务（GET方式，无需检查状态）")
    @GetMapping("/delivery/task/{orderId}/create")
    public Result<String> directCreateDelivery(@PathVariable Long orderId) {
        log.info("直接创建配送任务 - 订单ID: {}", orderId);
        try {
            Long riderId = 1L;
            deliveryService.createDeliveryTask(orderId, riderId);
            deliveryService.startDelivery(orderId);
            return Result.success("直接创建配送任务成功");
        } catch (Exception e) {
            log.error("创建配送任务失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "测试接口：直接创建配送任务（POST方式）")
    @PostMapping("/delivery/task/{orderId}/create")
    public Result<String> directCreateDeliveryPost(@PathVariable Long orderId) {
        log.info("直接创建配送任务（POST） - 订单ID: {}", orderId);
        try {
            Long riderId = 1L;
            deliveryService.createDeliveryTask(orderId, riderId);
            deliveryService.startDelivery(orderId);
            return Result.success("直接创建配送任务成功");
        } catch (Exception e) {
            log.error("创建配送任务失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取商家统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestHeader("Authorization") String token,
            @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        
        Map<String, Object> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
        
        // 今日营收
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> revenueWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        revenueWrapper.eq(Order::getMerchantId, merchantId)
                .ge(Order::getCreateTime, startOfToday)
                .lt(Order::getCreateTime, endOfToday)
                .in(Order::getStatus, Arrays.asList(2, 3, 4));
        List<Order> todayOrders = orderMapper.selectList(revenueWrapper);
        double todayRevenue = todayOrders.stream()
                .mapToDouble(o -> o.getFinalAmount().doubleValue())
                .sum();
        result.put("todayRevenue", todayRevenue);
        
        // 今日订单数
        result.put("todayOrders", todayOrders.size());
        
        // 店铺评分
        Merchant merchant = merchantMapper.selectById(merchantId);
        result.put("rating", merchant != null ? merchant.getAvgRating().doubleValue() : 5.0);
        
        // 总销量
        result.put("totalSales", merchant != null ? merchant.getSalesVolume() : 0);
        
        // 待接单数量
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> pendingWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        pendingWrapper.eq(Order::getMerchantId, merchantId)
                .eq(Order::getStatus, 1);
        result.put("pendingOrders", orderMapper.selectCount(pendingWrapper));
        
        // 配送中数量
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> deliveringWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        deliveringWrapper.eq(Order::getMerchantId, merchantId)
                .eq(Order::getStatus, 3);
        result.put("deliveringOrders", orderMapper.selectCount(deliveringWrapper));
        
        // 待回复评价数
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Review> reviewWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getMerchantId, merchantId)
                .isNull(Review::getReplyContent);
        result.put("pendingReviews", reviewMapper.selectCount(reviewWrapper));
        
        // 热门菜品
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FoodItem> foodWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        foodWrapper.eq(FoodItem::getMerchantId, merchantId)
                .orderByDesc(FoodItem::getSalesVolume)
                .last("LIMIT 5");
        List<FoodItem> topFoods = foodItemMapper.selectList(foodWrapper);
        result.put("topFoods", topFoods);
        
        // 营收趋势（近days天）
        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> dayWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            dayWrapper.eq(Order::getMerchantId, merchantId)
                    .ge(Order::getCreateTime, dayStart)
                    .lt(Order::getCreateTime, dayEnd)
                    .in(Order::getStatus, Arrays.asList(2, 3, 4));
            List<Order> dayOrders = orderMapper.selectList(dayWrapper);
            double dayRevenue = dayOrders.stream()
                    .mapToDouble(o -> o.getFinalAmount().doubleValue())
                    .sum();
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            dayData.put("revenue", dayRevenue);
            revenueTrend.add(dayData);
        }
        result.put("revenueTrend", revenueTrend);
        
        return Result.success(result);
    }

    @Operation(summary = "获取店铺分类列表")
    @GetMapping("/shop-categories")
    public Result<List<com.fooddelivery.entity.MerchantCategory>> getShopCategories() {
        List<com.fooddelivery.entity.MerchantCategory> categories = merchantCategoryService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取店铺信息")
    @GetMapping("/shop")
    public Result<Merchant> getShopInfo(@RequestHeader("Authorization") String token) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        Merchant merchant = merchantService.getMerchantById(merchantId);
        return Result.success(merchant);
    }

    @Operation(summary = "更新店铺信息")
    @PutMapping("/shop")
    public Result<String> updateShopInfo(@RequestHeader("Authorization") String token,
                                          @RequestBody Merchant merchant) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        merchant.setId(merchantId);
        merchantService.updateMerchant(merchant);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取菜品分类")
    @GetMapping("/food-categories")
    public Result<List<FoodCategory>> getFoodCategories(@RequestHeader("Authorization") String token) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        List<FoodCategory> categories = foodCategoryService.getCategoriesByMerchant(merchantId);
        return Result.success(categories);
    }

    @Operation(summary = "添加菜品分类")
    @PostMapping("/food-categories")
    public Result<FoodCategory> addFoodCategory(@RequestHeader("Authorization") String token,
                                                 @RequestBody FoodCategory category) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        category.setMerchantId(merchantId);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        foodCategoryService.addCategory(category);
        return Result.success("添加成功", category);
    }

    @Operation(summary = "删除菜品分类")
    @DeleteMapping("/food-categories/{categoryId}")
    public Result<String> deleteFoodCategory(@RequestHeader("Authorization") String token,
                                              @PathVariable Long categoryId) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        foodCategoryService.deleteCategory(categoryId, merchantId);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取菜品列表")
    @GetMapping("/foods")
    public Result<Page<FoodItem>> getFoods(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "菜品名称") @RequestParam(required = false) String foodName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        
        Page<FoodItem> pageObj = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FoodItem> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(FoodItem::getMerchantId, merchantId);
        
        if (foodName != null && !foodName.isEmpty()) {
            wrapper.like(FoodItem::getFoodName, foodName);
        }
        if (status != null) {
            wrapper.eq(FoodItem::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(FoodItem::getCategoryId, categoryId);
        }
        
        wrapper.orderByDesc(FoodItem::getCreateTime);
        
        Page<FoodItem> result = foodItemMapper.selectPage(pageObj, wrapper);
        
        return Result.success(result);
    }

    @Operation(summary = "获取菜品列表(全部)")
    @GetMapping("/foods/all")
    public Result<List<FoodItem>> getAllFoods(@RequestHeader("Authorization") String token) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FoodItem> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(FoodItem::getMerchantId, merchantId);
        wrapper.orderByDesc(FoodItem::getCreateTime);
        
        List<FoodItem> list = foodItemMapper.selectList(wrapper);
        
        return Result.success(list);
    }

    @Operation(summary = "获取菜品详情")
    @GetMapping("/foods/{id}")
    public Result<FoodItem> getFoodDetail(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "菜品ID") @PathVariable Long id) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        FoodItem food = foodItemMapper.selectById(id);
        
        if (food == null || !food.getMerchantId().equals(merchantId)) {
            return Result.error("菜品不存在");
        }
        
        return Result.success(food);
    }

    @Operation(summary = "添加菜品")
    @PostMapping("/foods")
    public Result<String> addFood(
            @RequestHeader("Authorization") String token,
            @RequestBody FoodItem foodItem) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        foodItem.setMerchantId(merchantId);
        foodItem.setSalesVolume(0);
        merchantService.addFoodItem(foodItem);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新菜品")
    @PutMapping("/foods/{id}")
    public Result<String> updateFood(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "菜品ID") @PathVariable Long id,
            @RequestBody FoodItem foodItem) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        FoodItem existing = foodItemMapper.selectById(id);
        
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            return Result.error("菜品不存在");
        }
        
        foodItem.setId(id);
        foodItem.setMerchantId(merchantId);
        merchantService.updateFoodItem(foodItem);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除菜品")
    @DeleteMapping("/foods/{id}")
    public Result<String> deleteFood(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "菜品ID") @PathVariable Long id) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        FoodItem existing = foodItemMapper.selectById(id);
        
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            return Result.error("菜品不存在");
        }
        
        merchantService.deleteFoodItem(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "更新菜品状态")
    @PutMapping("/foods/{id}/status")
    public Result<String> updateFoodStatus(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "菜品ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        FoodItem existing = foodItemMapper.selectById(id);
        
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            return Result.error("菜品不存在");
        }
        
        existing.setStatus(status);
        foodItemMapper.updateById(existing);
        return Result.success(status == 1 ? "已上架" : "已下架");
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/orders/{id}")
    public Result<OrderVO> getOrderDetail(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        Order order = orderMapper.selectById(id);

        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return Result.error("订单不存在");
        }

        OrderVO vo = orderService.buildOrderVO(order);

        return Result.success(vo);
    }

    @Operation(summary = "商家完成订单")
    @PostMapping("/orders/{id}/complete")
    public Result<String> completeOrder(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        Order order = orderMapper.selectById(id);
        
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return Result.error("订单不存在");
        }
        
        if (order.getStatus() != 2) {
            return Result.error("订单状态不正确");
        }
        
        order.setStatus(4);
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        return Result.success("订单已完成");
    }

    @Operation(summary = "商家取消订单")
    @PostMapping("/orders/{id}/cancel")
    public Result<String> cancelOrder(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        Order order = orderMapper.selectById(id);
        
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return Result.error("订单不存在");
        }
        
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            return Result.error("订单已处理，无法取消");
        }
        
        order.setStatus(5);
        order.setCancelReason("商家取消");
        orderMapper.updateById(order);
        
        return Result.success("订单已取消");
    }

    @Operation(summary = "获取评价列表")
    @GetMapping("/reviews")
    public Result<Page<Map<String, Object>>> getReviews(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "审核状态: 0-待审核, 1-已通过, 2-已拒绝，不传则默认返回已通过的") @RequestParam(required = false) Integer status,
            @Parameter(description = "评分") @RequestParam(required = false) Integer rating,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        
        Page<Review> pageObj = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Review> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Review::getMerchantId, merchantId);
        
        // 默认只返回已通过审核的评价
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        } else {
            wrapper.eq(Review::getStatus, 1); // 默认只显示已通过
        }
        
        if (rating != null) {
            wrapper.eq(Review::getRating, rating);
        }
        
        wrapper.orderByDesc(Review::getCreateTime);
        
        Page<Review> reviewPage = reviewMapper.selectPage(pageObj, wrapper);
        
        // 为每个评价添加用户信息和订单详情
        Page<Map<String, Object>> result = new Page<>(page, size);
        result.setTotal(reviewPage.getTotal());
        result.setCurrent(reviewPage.getCurrent());
        result.setSize(reviewPage.getSize());
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (Review review : reviewPage.getRecords()) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("orderId", review.getOrderId());
            reviewMap.put("userId", review.getUserId());
            reviewMap.put("merchantId", review.getMerchantId());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("tasteRating", review.getTasteRating());
            reviewMap.put("portionRating", review.getPortionRating());
            reviewMap.put("content", review.getContent());
            reviewMap.put("isAnonymous", review.getIsAnonymous());
            reviewMap.put("status", review.getStatus());
            reviewMap.put("replyContent", review.getReplyContent());
            reviewMap.put("replyTime", review.getReplyTime());
            reviewMap.put("createTime", review.getCreateTime());
            
            // 获取用户信息
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                reviewMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getPhone());
                reviewMap.put("userAvatar", user.getAvatar());
                reviewMap.put("userPhone", user.getPhone());
            }
            
            // 获取订单详情
            if (review.getOrderId() != null) {
                Order order = orderMapper.selectById(review.getOrderId());
                if (order != null) {
                    reviewMap.put("orderNo", order.getOrderNo());
                    reviewMap.put("orderStatus", order.getStatus());
                    reviewMap.put("orderCreateTime", order.getCreateTime());
                    reviewMap.put("orderFinalAmount", order.getFinalAmount());
                    
                    // 获取订单中的菜品
                    List<OrderItem> orderItems = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, review.getOrderId())
                    );
                    if (orderItems != null && !orderItems.isEmpty()) {
                        List<String> foodNames = new ArrayList<>();
                        for (OrderItem item : orderItems) {
                            FoodItem food = foodItemMapper.selectById(item.getFoodId());
                            if (food != null) {
                                foodNames.add(food.getFoodName() + " x" + item.getQuantity());
                            }
                        }
                        reviewMap.put("orderFoodNames", String.join(", ", foodNames));
                        reviewMap.put("foodName", foodNames.isEmpty() ? "" : foodNames.get(0)); // 兼容旧字段
                    }
                }
            }
            
            records.add(reviewMap);
        }
        result.setRecords(records);
        
        return Result.success(result);
    }

    @Operation(summary = "回复评价")
    @PostMapping("/reviews/{id}/reply")
    public Result<String> replyReview(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "评价ID") @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        String reply = params.get("reply");
        
        Review review = reviewMapper.selectById(id);
        if (review == null || !review.getMerchantId().equals(merchantId)) {
            return Result.error("评价不存在");
        }
        
        // 只有已通过审核的评价才能回复
        if (review.getStatus() == null || review.getStatus() != 1) {
            return Result.error("只能回复已通过审核的评价");
        }
        
        // 检查是否已回复
        if (review.getReplyContent() != null && !review.getReplyContent().isEmpty()) {
            return Result.error("该评价已回复");
        }
        
        review.setReplyContent(reply);
        review.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);
        
        return Result.success("回复成功");
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password")
    public Result<String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> params) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        Merchant merchant = merchantMapper.selectById(merchantId);
        String encryptedOld = com.fooddelivery.utils.MD5Util.encrypt(oldPassword);
        
        if (!encryptedOld.equals(merchant.getPassword())) {
            return Result.error("原密码错误");
        }
        
        merchant.setPassword(com.fooddelivery.utils.MD5Util.encrypt(newPassword));
        merchantMapper.updateById(merchant);
        
        return Result.success("密码修改成功");
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/sms/send")
    public Result<String> sendSms(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        merchantService.sendVerifyCode(phone);
        return Result.success("验证码已发送");
    }

    @Operation(summary = "更换手机号")
    @PostMapping("/phone")
    public Result<String> changePhone(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> params) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        String phone = params.get("phone");
        String code = params.get("code");
        
        // 验证验证码
        Map<String, String> codeMap = new java.util.HashMap<>();
        String savedCode = codeMap.get(phone);
        if (savedCode == null || !savedCode.equals(code)) {
            return Result.error("验证码错误");
        }
        
        // 检查手机号是否已被使用
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getPhone, phone);
        Merchant existing = merchantMapper.selectOne(wrapper);
        
        if (existing != null && !existing.getId().equals(merchantId)) {
            return Result.error("手机号已被使用");
        }
        
        Merchant merchant = merchantMapper.selectById(merchantId);
        merchant.setPhone(phone);
        merchantMapper.updateById(merchant);
        
        return Result.success("手机号更换成功");
    }
}
