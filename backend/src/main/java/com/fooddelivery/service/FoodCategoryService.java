package com.fooddelivery.service;

import com.fooddelivery.controller.MerchantSettingsDTO;
import com.fooddelivery.entity.FoodCategory;

import java.util.List;

/**
 * 餐品分类服务接口
 */
public interface FoodCategoryService {

    /**
     * 创建分类
     */
    void createCategory(FoodCategory category);

    /**
     * 更新分类
     */
    void updateCategory(FoodCategory category);

    /**
     * 删除分类
     */
    void deleteCategory(Long categoryId, Long merchantId);

    /**
     * 获取商家所有分类
     */
    List<FoodCategory> getMerchantCategories(Long merchantId);

    /**
     * 获取所有分类
     */
    List<FoodCategory> getAllCategories();

    /**
     * 根据ID获取分类
     */
    FoodCategory getById(Long categoryId);

    /**
     * 添加分类（商家端）
     */
    void addCategory(FoodCategory category);

    /**
     * 删除分类（商家端）
     */
    void deleteCategory(Long categoryId);

    /**
     * 获取商家分类列表
     */
    List<FoodCategory> getCategoriesByMerchant(Long merchantId);

    /**
     * 更新商家营业设置
     */
    void updateMerchantSettings(Long merchantId, MerchantSettingsDTO dto);
}
