package com.fooddelivery.controller;

import com.fooddelivery.dto.DeliveryCreateDTO;
import com.fooddelivery.dto.RiderLocationDTO;
import com.fooddelivery.entity.DeliveryTask;
import com.fooddelivery.service.DeliveryService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "创建配送任务")
    @PostMapping("/task/create")
    public Result<DeliveryTask> createTask(@RequestBody DeliveryCreateDTO dto) {
        DeliveryTask task = deliveryService.createDeliveryTask(dto.getOrderId(), dto.getRiderId());
        return Result.success(task);
    }

    @Operation(summary = "开始配送")
    @PostMapping("/task/{orderId}/start")
    public Result<String> startDelivery(@PathVariable Long orderId) {
        deliveryService.startDelivery(orderId);
        return Result.success("配送已开始");
    }

    @Operation(summary = "完成配送")
    @PostMapping("/task/{orderId}/complete")
    public Result<String> completeDelivery(@PathVariable Long orderId) {
        deliveryService.completeDelivery(orderId);
        return Result.success("配送完成");
    }

    @Operation(summary = "获取配送任务")
    @GetMapping("/task/{orderId}")
    public Result<DeliveryTask> getDeliveryTask(@PathVariable Long orderId) {
        DeliveryTask task = deliveryService.getDeliveryTaskByOrderId(orderId);
        return Result.success(task);
    }

    @Operation(summary = "更新骑手位置")
    @PostMapping("/task/{orderId}/location")
    public Result<String> updateRiderLocation(@PathVariable Long orderId, @RequestBody RiderLocationDTO location) {
        deliveryService.updateRiderLocation(orderId, location);
        return Result.success("位置更新已发送");
    }

    @Operation(summary = "获取骑手位置")
    @GetMapping("/task/{orderId}/location")
    public Result<RiderLocationDTO> getRiderLocation(@PathVariable Long orderId) {
        RiderLocationDTO location = deliveryService.getRiderLocation(orderId);
        return Result.success(location);
    }

    @Operation(summary = "获取商家配送任务列表")
    @GetMapping("/merchant/tasks")
    public Result<List<DeliveryTask>> getMerchantDeliveryTasks(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        List<DeliveryTask> tasks = deliveryService.getMerchantDeliveryTasks(merchantId);
        return Result.success(tasks);
    }

    @Operation(summary = "分配骑手")
    @PostMapping("/task/{orderId}/assign")
    public Result<String> assignRider(@PathVariable Long orderId, @RequestParam Long riderId) {
        deliveryService.assignRider(orderId, riderId);
        return Result.success("骑手分配成功");
    }

    // 管理端接口：获取所有配送任务
    @Operation(summary = "管理端获取所有配送任务")
    @GetMapping("/admin/tasks")
    public Result<List<DeliveryTask>> getAllDeliveryTasks() {
        return Result.success(deliveryService.getAllDeliveryTasks());
    }
    
    @Operation(summary = "手动触发配送位置更新（测试用）")
    @PostMapping("/task/{orderId}/update-position")
    public Result<String> manualUpdatePosition(@PathVariable Long orderId) {
        deliveryService.updateDeliveryPosition(orderId);
        return Result.success("位置更新成功");
    }
}
