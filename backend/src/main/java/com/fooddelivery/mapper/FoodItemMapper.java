package com.fooddelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fooddelivery.entity.FoodItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 餐品Mapper接口
 */
@Mapper
public interface FoodItemMapper extends BaseMapper<FoodItem> {
}
