package com.fooddelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.CreateOrderDTO;
import com.fooddelivery.entity.Order;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.OrderVO;
import com.fooddelivery.vo.Result;
import com.fooddelivery.websocket.WebSocketServer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单Controller
 */
@Slf4j
@Tag(name = "订单接口", description = "订单相关操作接口")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestHeader("Authorization") String token,
                                                   @RequestBody @Valid CreateOrderDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        Order order = orderService.createOrder(userId, dto);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("finalAmount", order.getFinalAmount());

        return Result.success("创建订单成功", data);
    }

    @Operation(summary = "支付订单")
    @PostMapping("/pay/{orderId}")
    public Result<String> payOrder(@RequestHeader("Authorization") String token,
                                   @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        orderService.payOrder(orderId, userId);

        // 订阅订单更新
        WebSocketServer.subscribeOrder(orderId, userId);

        return Result.success("支付成功");
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel/{orderId}")
    public Result<String> cancelOrder(@RequestHeader("Authorization") String token,
                                      @Parameter(description = "订单ID") @PathVariable Long orderId,
                                      @Parameter(description = "取消原因") @RequestParam String reason) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        orderService.cancelOrder(orderId, userId, reason);
        return Result.success("取消订单成功");
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(@RequestHeader("Authorization") String token,
                                          @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        OrderVO order = orderService.getOrderDetail(orderId, userId);
        return Result.success(order);
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping("/list")
    public Result<Page<OrderVO>> getUserOrders(@RequestHeader("Authorization") String token,
                                               @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
                                               @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
                                               @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        Page<OrderVO> page = orderService.getUserOrders(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "确认订单完成")
    @PostMapping("/confirm/{orderId}")
    public Result<String> confirmComplete(@RequestHeader("Authorization") String token,
                                          @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        orderService.confirmComplete(orderId, userId);
        return Result.success("确认完成");
    }
}