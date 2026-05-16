package com.fooddelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fooddelivery.entity.CartItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * CartItem 数据访问接口（持久化购物车）
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItemEntity> {
}
