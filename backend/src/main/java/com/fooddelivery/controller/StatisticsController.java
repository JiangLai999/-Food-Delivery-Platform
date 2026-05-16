package com.fooddelivery.controller;

import com.fooddelivery.service.StatisticsService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.ChartDataVO;
import com.fooddelivery.vo.Result;
import com.fooddelivery.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计Controller
 */
@Slf4j
@Tag(name = "统计接口", description = "数据统计分析接口")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "获取平台统计数据")
    @GetMapping("/platform")
    public Result<StatisticsVO> getPlatformStatistics() {
        StatisticsVO vo = statisticsService.getPlatformStatistics();
        return Result.success(vo);
    }

    @Operation(summary = "获取商家统计数据")
    @GetMapping("/merchant")
    public Result<StatisticsVO> getMerchantStatistics(@RequestHeader("Authorization") String token) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        StatisticsVO vo = statisticsService.getMerchantStatistics(merchantId);
        return Result.success(vo);
    }

    @Operation(summary = "获取日订单量统计")
    @GetMapping("/daily-orders")
    public Result<ChartDataVO> getDailyOrderCount(@RequestHeader(value = "Authorization", required = false) String token,
                                                  @Parameter(description = "商家ID") @RequestParam(required = false) Long merchantId,
                                                  @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        if (token != null && merchantId == null) {
            merchantId = jwtUtil.getUserIdFromToken(token);
        }
        ChartDataVO chartData = statisticsService.getDailyOrderCount(merchantId, days);
        return Result.success(chartData);
    }

    @Operation(summary = "获取日销售额统计")
    @GetMapping("/daily-sales")
    public Result<ChartDataVO> getDailySales(@RequestHeader(value = "Authorization", required = false) String token,
                                             @Parameter(description = "商家ID") @RequestParam(required = false) Long merchantId,
                                             @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        if (token != null && merchantId == null) {
            merchantId = jwtUtil.getUserIdFromToken(token);
        }
        ChartDataVO chartData = statisticsService.getDailySales(merchantId, days);
        return Result.success(chartData);
    }

    @Operation(summary = "获取热销菜品Top10")
    @GetMapping("/top-foods")
    public Result<List<Map<String, Object>>> getTopFoods(@RequestHeader("Authorization") String token,
                                                         @Parameter(description = "返回数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        List<Map<String, Object>> topFoods = statisticsService.getTopFoods(merchantId, limit);
        return Result.success(topFoods);
    }

    @Operation(summary = "获取商家活跃度统计")
    @GetMapping("/merchant-activity")
    public Result<ChartDataVO> getMerchantActivityStatistics() {
        ChartDataVO chartData = statisticsService.getMerchantActivityStatistics();
        return Result.success(chartData);
    }

    @Operation(summary = "获取订单状态分布")
    @GetMapping("/order-status-distribution")
    public Result<Map<String, Long>> getOrderStatusDistribution(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "商家ID") @RequestParam(required = false) Long merchantId) {
        if (token != null && merchantId == null) {
            merchantId = jwtUtil.getUserIdFromToken(token);
        }
        Map<String, Long> distribution = statisticsService.getOrderStatusDistribution(merchantId);
        return Result.success(distribution);
    }

    @Operation(summary = "获取热卖商家Top(N)")
    @GetMapping("/top-merchants")
    public Result<List<Map<String, Object>>> getTopMerchants(@RequestParam(required = false) Integer limit) {
        List<Map<String, Object>> list = statisticsService.getTopMerchants(limit);
        return Result.success(list);
    }

    @Operation(summary = "获取用户增长趋势")
    @GetMapping("/user-growth")
    public Result<ChartDataVO> getUserGrowthTrend(
            @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        ChartDataVO chartData = statisticsService.getUserGrowthTrend(days);
        return Result.success(chartData);
    }

    @Operation(summary = "获取商家分类分布")
    @GetMapping("/merchant-category")
    public Result<List<Map<String, Object>>> getMerchantCategoryDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getMerchantCategoryDistribution();
        return Result.success(distribution);
    }

    @Operation(summary = "获取区域订单分布")
    @GetMapping("/region-distribution")
    public Result<List<Map<String, Object>>> getRegionOrderDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getRegionOrderDistribution();
        return Result.success(distribution);
    }

    @Operation(summary = "获取餐品分类销量分布")
    @GetMapping("/food-category-sales")
    public Result<List<Map<String, Object>>> getFoodCategorySalesDistribution(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "商家ID") @RequestParam(required = false) Long merchantId) {
        if (token != null && merchantId == null) {
            merchantId = jwtUtil.getUserIdFromToken(token);
        }
        List<Map<String, Object>> distribution = statisticsService.getFoodCategorySalesDistribution(merchantId);
        return Result.success(distribution);
    }

    @Operation(summary = "获取平台订单趋势")
    @GetMapping("/platform-trend")
    public Result<Map<String, Object>> getPlatformOrderTrend(
            @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> trend = statisticsService.getPlatformOrderTrend(days);
        return Result.success(trend);
    }

    @Operation(summary = "获取完整平台统计数据（包含所有图表数据）")
    @GetMapping("/full-platform")
    public Result<Map<String, Object>> getFullPlatformStatistics(
            @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> result = statisticsService.getFullPlatformStatistics();
        result.put("trendDays", days);
        return Result.success(result);
    }
}
