package com.fooddelivery.controller;

import com.fooddelivery.dto.RiderLocationDTO;
import com.fooddelivery.entity.Rider;
import com.fooddelivery.service.RiderService;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 骑手Controller
 */
@Slf4j
@Tag(name = "骑手接口", description = "骑手相关操作接口")
@RestController
@RequestMapping("/rider")
public class RiderController {

    @Autowired
    private RiderService riderService;

    @Operation(summary = "获取骑手位置")
    @GetMapping("/location/{riderId}")
    public Result<RiderLocationDTO> getRiderLocation(
            @Parameter(description = "骑手ID") @PathVariable Long riderId) {
        RiderLocationDTO location = riderService.getRiderLocation(riderId);
        return Result.success(location);
    }

    @Operation(summary = "获取所有骑手")
    @GetMapping("/list")
    public Result<List<Rider>> getAllRiders() {
        List<Rider> riders = riderService.getAllRiders();
        return Result.success(riders);
    }

    @Operation(summary = "创建骑手")
    @PostMapping("/create")
    public Result<String> createRider(@RequestBody Rider rider) {
        riderService.createRider(rider);
        return Result.success("创建成功");
    }
}