package com.fooddelivery.service;

import com.fooddelivery.dto.RiderLocationDTO;
import com.fooddelivery.entity.Rider;

import java.util.List;

/**
 * 骑手服务接口
 */
public interface RiderService {

    /**
     * 获取可用骑手
     */
    Rider getAvailableRider();

    /**
     * 分配骑手到订单
     */
    void assignRiderToOrder(Long orderId, Long riderId);

    /**
     * 获取骑手当前位置
     */
    RiderLocationDTO getRiderLocation(Long riderId);

    /**
     * 更新骑手位置
     */
    void updateRiderLocation(Long riderId, RiderLocationDTO locationDTO);

    /**
     * 获取所有骑手列表
     */
    List<Rider> getAllRiders();

    /**
     * 创建骑手
     */
    void createRider(Rider rider);
}
