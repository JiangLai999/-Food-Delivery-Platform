package com.fooddelivery.controller;

import com.fooddelivery.service.RecommendationService;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "推荐接口", description = "基于简单协同过滤的推荐接口")
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Operation(summary = "获取推荐餐品（基于用户历史）")
    @GetMapping("/foods")
    public Result<List<Map<String, Object>>> getRecommendedFoods(@RequestParam(required = false) Long userId,
                                                               @RequestParam(required = false, defaultValue = "5") Integer limit) {
        List<Map<String, Object>> foods = recommendationService.getRecommendedFoods(userId, limit);
        return Result.success(foods);
    }

    @Operation(summary = "获取热卖商家Top N")
    @GetMapping("/top-merchants")
    public Result<List<Map<String, Object>>> getTopMerchants(@RequestParam(required = false, defaultValue = "5") Integer limit) {
        List<Map<String, Object>> merchants = recommendationService.getTopMerchants(limit);
        return Result.success(merchants);
    }

    @Operation(summary = "获取推荐商家（协同过滤）")
    @GetMapping("/merchants")
    public Result<List<Map<String, Object>>> getRecommendedMerchants(@RequestParam(required = false) Long userId,
                                                                  @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> merchants = recommendationService.getRecommendedMerchants(userId, limit);
        return Result.success(merchants);
    }
}
