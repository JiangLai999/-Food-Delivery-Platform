package com.fooddelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fooddelivery.entity.MerchantCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家分类Mapper接口
 */
@Mapper
public interface MerchantCategoryMapper extends BaseMapper<MerchantCategory> {
}
