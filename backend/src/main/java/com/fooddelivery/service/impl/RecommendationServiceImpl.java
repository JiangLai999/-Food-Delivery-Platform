package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OrderItem;
import com.fooddelivery.mapper.FoodItemMapper;
import com.fooddelivery.mapper.MerchantMapper;
import com.fooddelivery.mapper.OrderItemMapper;
import com.fooddelivery.mapper.OrderMapper;
import com.fooddelivery.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐实现
 * 1. 基于用户的协同过滤：找到相似用户喜欢的商品
 * 2. 基于物品的协同过滤：找到用户历史购买商品的相似商品
 * 3. 基于内容的推荐：根据用户历史偏好推荐同类型商品
 */
@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public List<Map<String, Object>> getRecommendedFoods(Long userId, Integer limit) {
        if (userId == null) {
            return getPopularFoods(limit);
        }
        
        Map<Long, Double> userPreferences = buildUserPreferenceVector(userId);
        
        if (userPreferences.isEmpty()) {
            return getPopularFoods(limit);
        }
        
        List<FoodItem> allFoods = foodItemMapper.selectList(
            new LambdaQueryWrapper<FoodItem>().eq(FoodItem::getStatus, 1));
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        for (FoodItem food : allFoods) {
            if (userPreferences.containsKey(food.getId())) {
                continue;
            }
            
            double score = calculateFoodScore(food, userPreferences);
            Map<String, Object> map = new HashMap<>();
            map.put("foodId", food.getId());
            map.put("foodName", food.getFoodName());
            map.put("price", food.getPrice());
            map.put("salesVolume", food.getSalesVolume());
            map.put("merchantId", food.getMerchantId());
            map.put("recommendationScore", score);
            recommendations.add(map);
        }
        
        recommendations.sort((a, b) -> {
            double scoreA = ((Number) a.getOrDefault("recommendationScore", 0.0)).doubleValue();
            double scoreB = ((Number) b.getOrDefault("recommendationScore", 0.0)).doubleValue();
            return Double.compare(scoreB, scoreA);
        });
        
        int resultSize = limit != null ? Math.min(limit, recommendations.size()) : recommendations.size();
        return recommendations.subList(0, resultSize);
    }

    private Map<Long, Double> buildUserPreferenceVector(Long userId) {
        Map<Long, Double> preferences = new HashMap<>();
        
        List<Order> userOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getPayStatus, 1));
        
        for (Order order : userOrders) {
            List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            
            for (OrderItem item : items) {
                Long foodId = item.getFoodId();
                Double currentScore = preferences.get(foodId);
                double newScore = (currentScore != null ? currentScore : 0) + item.getQuantity() * 2.0;
                preferences.put(foodId, newScore);
            }
            
            Merchant merchant = merchantMapper.selectById(order.getMerchantId());
            if (merchant != null && merchant.getCategoryId() != null) {
                List<FoodItem> categoryFoods = foodItemMapper.selectList(
                    new LambdaQueryWrapper<FoodItem>()
                        .eq(FoodItem::getMerchantId, merchant.getId())
                        .eq(FoodItem::getStatus, 1));
                
                for (FoodItem food : categoryFoods) {
                    Double currentScore = preferences.get(food.getId());
                    double newScore = (currentScore != null ? currentScore : 0) + 1.0;
                    preferences.put(food.getId(), newScore);
                }
            }
        }
        
        return preferences;
    }

    private double calculateFoodScore(FoodItem food, Map<Long, Double> userPreferences) {
        double score = 0;
        
        Long merchantId = food.getMerchantId();
        if (merchantId != null) {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant != null && merchant.getCategoryId() != null) {
                for (Map.Entry<Long, Double> entry : userPreferences.entrySet()) {
                    FoodItem purchasedFood = foodItemMapper.selectById(entry.getKey());
                    if (purchasedFood != null && 
                        purchasedFood.getMerchantId() != null &&
                        purchasedFood.getMerchantId().equals(merchantId)) {
                        score += entry.getValue() * 0.3;
                    }
                }
            }
        }
        
        if (food.getSalesVolume() != null) {
            score += Math.log(food.getSalesVolume() + 1) * 0.5;
        }
        
        Merchant merchant = merchantMapper.selectById(food.getMerchantId());
        if (merchant != null && merchant.getAvgRating() != null) {
            score += merchant.getAvgRating().doubleValue() * 0.3;
        }
        
        return score;
    }

    private List<Map<String, Object>> getPopularFoods(Integer limit) {
        List<FoodItem> topFoods = foodItemMapper.selectList(
            new LambdaQueryWrapper<FoodItem>()
                .eq(FoodItem::getStatus, 1)
                .orderByDesc(FoodItem::getSalesVolume)
                .last("LIMIT " + (limit != null ? limit : 10)));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (FoodItem f : topFoods) {
            Map<String, Object> map = new HashMap<>();
            map.put("foodId", f.getId());
            map.put("foodName", f.getFoodName());
            map.put("price", f.getPrice());
            map.put("salesVolume", f.getSalesVolume());
            map.put("merchantId", f.getMerchantId());
            map.put("recommendationScore", f.getSalesVolume());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTopMerchants(Integer limit) {
        List<Merchant> list = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, 1)
                        .orderByDesc(Merchant::getSalesVolume)
                        .last("LIMIT " + (limit == null ? 5 : limit)));
        List<Map<String, Object>> res = new ArrayList<>();
        if (list != null) {
            for (Merchant m : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("merchantId", m.getId());
                map.put("merchantName", m.getMerchantName());
                map.put("salesVolume", m.getSalesVolume());
                map.put("avgRating", m.getAvgRating());
                res.add(map);
            }
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> getRecommendedMerchants(Long userId, Integer limit) {
        if (userId == null) {
            return getTopMerchants(limit);
        }
        
        Map<Long, Integer> categoryPreferences = new HashMap<>();
        Map<Long, Integer> merchantPreferences = new HashMap<>();
        
        List<Order> userOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getPayStatus, 1));
        
        for (Order order : userOrders) {
            merchantPreferences.merge(order.getMerchantId(), 1, Integer::sum);
            
            Merchant merchant = merchantMapper.selectById(order.getMerchantId());
            if (merchant != null && merchant.getCategoryId() != null) {
                categoryPreferences.merge(merchant.getCategoryId(), 2, Integer::sum);
            }
        }
        
        List<Merchant> allMerchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, 1));
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Merchant m : allMerchants) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("merchantId", m.getId());
            map.put("merchantName", m.getMerchantName());
            map.put("categoryId", m.getCategoryId());
            map.put("avgRating", m.getAvgRating());
            map.put("salesVolume", m.getSalesVolume());
            
            double score = 0;
            
            Integer merchantFreq = merchantPreferences.get(m.getId());
            if (merchantFreq != null) {
                score += merchantFreq * 10.0;
            }
            
            if (m.getCategoryId() != null) {
                Integer categoryFreq = categoryPreferences.get(m.getCategoryId());
                if (categoryFreq != null) {
                    score += categoryFreq * 5.0;
                }
            }
            
            if (m.getAvgRating() != null) {
                score += m.getAvgRating().doubleValue() * 2.0;
            }
            
            if (m.getSalesVolume() != null) {
                score += Math.log(m.getSalesVolume() + 1) * 0.5;
            }
            
            map.put("recommendationScore", score);
            result.add(map);
        }
        
        result.sort((a, b) -> {
            double scoreA = ((Number) a.getOrDefault("recommendationScore", 0.0)).doubleValue();
            double scoreB = ((Number) b.getOrDefault("recommendationScore", 0.0)).doubleValue();
            return Double.compare(scoreB, scoreA);
        });
        
        int resultLimit = limit != null ? limit : 10;
        return result.stream().limit(resultLimit).collect(Collectors.toList());
    }
}
