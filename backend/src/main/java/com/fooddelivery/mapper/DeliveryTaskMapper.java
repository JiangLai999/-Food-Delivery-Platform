package com.fooddelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fooddelivery.entity.DeliveryTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 配送任务Mapper接口
 */
@Mapper
public interface DeliveryTaskMapper extends BaseMapper<DeliveryTask> {
}
