package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.controller.MerchantSettingsDTO;
import com.fooddelivery.entity.FoodCategory;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.mapper.FoodCategoryMapper;
import com.fooddelivery.mapper.MerchantMapper;
import com.fooddelivery.service.FoodCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 餐品分类服务实现类
 */
@Slf4j
@Service
public class FoodCategoryServiceImpl implements FoodCategoryService {

    @Autowired
    private FoodCategoryMapper foodCategoryMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    @Transactional
    public void createCategory(FoodCategory category) {
        foodCategoryMapper.insert(category);
        log.info("创建餐品分类: {}", category.getCategoryName());
    }

    @Override
    @Transactional
    public void updateCategory(FoodCategory category) {
        foodCategoryMapper.updateById(category);
        log.info("更新餐品分类: {}", category.getId());
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId, Long merchantId) {
        FoodCategory category = foodCategoryMapper.selectById(categoryId);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("分类不存在或无权限");
        }

        foodCategoryMapper.deleteById(categoryId);
        log.info("删除餐品分类: {}", categoryId);
    }

    @Override
    public List<FoodCategory> getMerchantCategories(Long merchantId) {
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodCategory::getMerchantId, merchantId)
                .orderByAsc(FoodCategory::getSortOrder);
        return foodCategoryMapper.selectList(wrapper);
    }

    @Override
    public List<FoodCategory> getAllCategories() {
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FoodCategory::getSortOrder);
        return foodCategoryMapper.selectList(wrapper);
    }

    @Override
    public FoodCategory getById(Long categoryId) {
        return foodCategoryMapper.selectById(categoryId);
    }

    @Override
    @Transactional
    public void addCategory(FoodCategory category) {
        foodCategoryMapper.insert(category);
        log.info("商家添加分类: merchantId={}, name={}", category.getMerchantId(), category.getCategoryName());
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        foodCategoryMapper.deleteById(categoryId);
        log.info("商家删除分类: {}", categoryId);
    }

    @Override
    public List<FoodCategory> getCategoriesByMerchant(Long merchantId) {
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodCategory::getMerchantId, merchantId)
                .or()
                .eq(FoodCategory::getMerchantId, 0) // 通用分类
                .orderByAsc(FoodCategory::getSortOrder);
        return foodCategoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void updateMerchantSettings(Long merchantId, MerchantSettingsDTO dto) {
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setBusinessHours(dto.getBusinessHours());
        merchant.setDeliveryFee(dto.getDeliveryFee());
        merchant.setMinOrderAmount(dto.getMinOrderAmount());
        
        merchantMapper.updateById(merchant);
        log.info("商家更新营业设置: merchantId={}", merchantId);
    }
}
