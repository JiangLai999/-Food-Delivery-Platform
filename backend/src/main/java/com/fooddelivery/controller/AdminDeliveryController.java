package com.fooddelivery.controller;

import com.fooddelivery.dto.DeliveryCreateDTO;
import com.fooddelivery.entity.DeliveryTask;
import com.fooddelivery.service.DeliveryService;
import com.fooddelivery.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminDeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/delivery/tasks")
    public Result<List<DeliveryTask>> getDeliveryTasks() {
        List<DeliveryTask> tasks = deliveryService.getAllDeliveryTasks();
        return Result.success(tasks);
    }
}
