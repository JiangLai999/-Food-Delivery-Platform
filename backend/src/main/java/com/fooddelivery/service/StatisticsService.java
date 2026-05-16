package com.fooddelivery.service;

import com.fooddelivery.vo.ChartDataVO;
import com.fooddelivery.vo.StatisticsVO;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取平台统计数据
     */
    StatisticsVO getPlatformStatistics();

    /**
     * 获取商家统计数据
     */
    StatisticsVO getMerchantStatistics(Long merchantId);

    /**
     * 获取日订单量统计（最近7天或30天）
     */
    ChartDataVO getDailyOrderCount(Long merchantId, Integer days);

    /**
     * 获取日销售额统计
     */
    ChartDataVO getDailySales(Long merchantId, Integer days);

    /**
     * 获取热销菜品Top10
     */
    List<Map<String, Object>> getTopFoods(Long merchantId, Integer limit);

    /**
     * 获取商家活跃度统计
     */
    ChartDataVO getMerchantActivityStatistics();

    /**
     * 获取订单状态分布
     */
    Map<String, Long> getOrderStatusDistribution(Long merchantId);

    /**
     * 获取平台/全局热卖商家 Top N
     */
    List<Map<String, Object>> getTopMerchants(Integer limit);

    /**
     * 获取用户增长趋势（按天）
     */
    ChartDataVO getUserGrowthTrend(Integer days);

    /**
     * 获取商家分类分布
     */
    List<Map<String, Object>> getMerchantCategoryDistribution();

    /**
     * 获取区域订单分布
     */
    List<Map<String, Object>> getRegionOrderDistribution();

    /**
     * 获取餐品分类销量分布
     */
    List<Map<String, Object>> getFoodCategorySalesDistribution(Long merchantId);

    /**
     * 获取平台订单趋势（订单量和销售额）
     */
    Map<String, Object> getPlatformOrderTrend(Integer days);

    /**
     * 获取完整的平台统计数据（包含所有图表所需数据）
     */
    Map<String, Object> getFullPlatformStatistics();
}
