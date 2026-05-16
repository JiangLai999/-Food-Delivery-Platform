package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.entity.MerchantCategory;
import com.fooddelivery.mapper.MerchantCategoryMapper;
import com.fooddelivery.service.MerchantCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商家分类服务实现
 */
@Service
public class MerchantCategoryServiceImpl implements MerchantCategoryService {

    @Autowired
    private MerchantCategoryMapper merchantCategoryMapper;

    @Override
    public List<MerchantCategory> getAllCategories() {
        return merchantCategoryMapper.selectList(
            new LambdaQueryWrapper<MerchantCategory>()
                .orderByAsc(MerchantCategory::getSortOrder)
        );
    }
}
