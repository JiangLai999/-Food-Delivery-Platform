package com.fooddelivery.service;

import com.fooddelivery.entity.MerchantCategory;

import java.util.List;

/**
 * 商家分类服务接口
 */
public interface MerchantCategoryService {

    /**
     * 获取所有商家分类
     */
    List<MerchantCategory> getAllCategories();
}
