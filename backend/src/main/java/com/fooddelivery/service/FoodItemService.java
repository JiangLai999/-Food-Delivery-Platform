package com.fooddelivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.FoodItem;

import java.util.List;

/**
 * 餐品服务接口
 */
public interface FoodItemService {

    /**
     * 根据商家ID获取餐品列表
     */
    List<FoodItem> getFoodsByMerchantId(Long merchantId);

    /**
     * 根据分类获取餐品
     */
    List<FoodItem> getFoodsByCategory(Long categoryId);

    /**
     * 搜索餐品
     */
    Page<FoodItem> searchFoods(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取餐品详情
     */
    FoodItem getFoodById(Long foodId);

    /**
     * 获取热门餐品
     */
    List<FoodItem> getHotFoods(Integer limit);

    /**
     * 获取所有餐品列表（分页）
     */
    Page<FoodItem> getFoodList(Integer page, Integer pageSize);

    /**
     * 增加销量
     */
    void increaseSalesVolume(Long foodId, Integer quantity);

    /**
     * 根据关键词、商家、分类、排序筛选餐品
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.fooddelivery.entity.FoodItem> filterFoods(
            String keyword,
            Long merchantId,
            Long categoryId,
            String sort,
            Integer pageNum,
            Integer pageSize
    );

    /**
     * Update food item images (bulk set)
     */
    void updateFoodImages(Long foodId, java.util.List<String> images);
}
