package com.fooddelivery.service;

import java.util.List;
import java.util.Map;

/**
 * 推荐服务接口（简易实现的协同过滤占位）
 */
public interface RecommendationService {
    /**
     * 根据用户历史订单，生成推荐的餐品列表（具体实现为简化的频次统计）
     */
    List<Map<String, Object>> getRecommendedFoods(Long userId, Integer limit);
    
    /**
     * 获取热卖商家 Top N（供前端快速示例使用）
     */
    List<Map<String, Object>> getTopMerchants(Integer limit);

    /**
     * 根据用户历史订单，生成推荐的商家列表（协同过滤）
     */
    List<Map<String, Object>> getRecommendedMerchants(Long userId, Integer limit);
}
