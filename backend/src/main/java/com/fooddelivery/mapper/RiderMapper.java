package com.fooddelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fooddelivery.entity.Rider;
import org.apache.ibatis.annotations.Mapper;

/**
 * 骑手Mapper接口
 */
@Mapper
public interface RiderMapper extends BaseMapper<Rider> {
}
