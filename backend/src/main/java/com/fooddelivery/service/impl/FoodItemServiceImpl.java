package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.mapper.FoodItemMapper;
import com.fooddelivery.service.FoodItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 实现餐品相关业务的简单服务 (初步实现)
 */
@Service
public class FoodItemServiceImpl implements FoodItemService {

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Override
    public List<FoodItem> getFoodsByMerchantId(Long merchantId) {
        return foodItemMapper.selectList(new LambdaQueryWrapper<FoodItem>()
                .eq(FoodItem::getMerchantId, merchantId));
    }

    @Override
    public List<FoodItem> getFoodsByCategory(Long categoryId) {
        return foodItemMapper.selectList(new LambdaQueryWrapper<FoodItem>()
                .eq(FoodItem::getCategoryId, categoryId));
    }

    @Override
    public Page<FoodItem> searchFoods(String keyword, Integer pageNum, Integer pageSize) {
        Page<FoodItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoodItem> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(FoodItem::getFoodName, keyword);
        }
        wrapper.orderByDesc(FoodItem::getSalesVolume);
        return foodItemMapper.selectPage(page, wrapper);
    }

    @Override
    public FoodItem getFoodById(Long foodId) {
        return foodItemMapper.selectById(foodId);
    }

    @Override
    public List<FoodItem> getHotFoods(Integer limit) {
        return foodItemMapper.selectList(new LambdaQueryWrapper<FoodItem>()
                .eq(FoodItem::getStatus, 1)
                .orderByDesc(FoodItem::getSalesVolume)
                .last("LIMIT " + limit));
    }

    @Override
    public Page<FoodItem> getFoodList(Integer page, Integer pageSize) {
        Page<FoodItem> p = new Page<>(page, pageSize);
        return foodItemMapper.selectPage(p, new LambdaQueryWrapper<FoodItem>().orderByDesc(FoodItem::getCreateTime));
    }

    @Override
    public void increaseSalesVolume(Long foodId, Integer quantity) {
        FoodItem item = foodItemMapper.selectById(foodId);
        if (item != null) {
            int current = item.getSalesVolume() == null ? 0 : item.getSalesVolume();
            item.setSalesVolume(current + quantity);
            foodItemMapper.updateById(item);
        }
    }

    @Override
    public Page<FoodItem> filterFoods(String keyword, Long merchantId, Long categoryId, String sort, Integer pageNum, Integer pageSize) {
        Page<FoodItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoodItem> wrapper = new LambdaQueryWrapper<>();
        if (merchantId != null) wrapper.eq(FoodItem::getMerchantId, merchantId);
        if (categoryId != null) wrapper.eq(FoodItem::getCategoryId, categoryId);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(FoodItem::getFoodName, keyword);
        if (sort != null) {
            switch (sort) {
                case "sales_desc": wrapper.orderByDesc(FoodItem::getSalesVolume); break;
                case "price_asc": wrapper.orderByAsc(FoodItem::getPrice); break;
                case "price_desc": wrapper.orderByDesc(FoodItem::getPrice); break;
                default: wrapper.orderByDesc(FoodItem::getSalesVolume); break;
            }
        } else {
            wrapper.orderByDesc(FoodItem::getSalesVolume);
        }
        return foodItemMapper.selectPage(page, wrapper);
    }

    @Override
    public void updateFoodImages(Long foodId, List<String> images) {
        FoodItem food = foodItemMapper.selectById(foodId);
        if (food != null) {
            String csv = (images == null || images.isEmpty()) ? null : String.join(",", images);
            food.setImage(csv);
            foodItemMapper.updateById(food);
        }
    }
}
