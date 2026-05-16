package com.fooddelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fooddelivery.entity.RiderLocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 骑手位置记录Mapper接口
 */
@Mapper
public interface RiderLocationMapper extends BaseMapper<RiderLocation> {
}
