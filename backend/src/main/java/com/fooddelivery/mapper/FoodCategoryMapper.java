package com.fooddelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fooddelivery.entity.FoodCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 餐品分类Mapper接口
 */
@Mapper
public interface FoodCategoryMapper extends BaseMapper<FoodCategory> {
}
