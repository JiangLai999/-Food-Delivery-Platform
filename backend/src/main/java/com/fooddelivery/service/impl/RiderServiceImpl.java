package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.dto.RiderLocationDTO;
import com.fooddelivery.entity.Rider;
import com.fooddelivery.entity.RiderLocation;
import com.fooddelivery.mapper.RiderLocationMapper;
import com.fooddelivery.mapper.RiderMapper;
import com.fooddelivery.service.RiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 骑手服务实现类
 */
@Slf4j
@Service
public class RiderServiceImpl implements RiderService {

    @Autowired
    private RiderMapper riderMapper;

    @Autowired
    private RiderLocationMapper riderLocationMapper;

    @Override
    public Rider getAvailableRider() {
        // 查询状态为待接单的骑手
        LambdaQueryWrapper<Rider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Rider::getStatus, 1).last("LIMIT 1");
        Rider rider = riderMapper.selectOne(wrapper);

        if (rider == null) {
            log.warn("当前没有可用的骑手");
        }

        return rider;
    }

    @Override
    @Transactional
    public void assignRiderToOrder(Long orderId, Long riderId) {
        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            throw new RuntimeException("骑手不存在");
        }

        // 更新骑手状态为配送中
        rider.setStatus(2);
        riderMapper.updateById(rider);

        log.info("骑手{}已分配到订单{}", riderId, orderId);
    }

    @Override
    public RiderLocationDTO getRiderLocation(Long riderId) {
        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            return null;
        }

        RiderLocationDTO dto = new RiderLocationDTO();
        dto.setRiderId(rider.getId());
        dto.setRiderName(rider.getRiderName());
        dto.setLongitude(rider.getCurrentLongitude());
        dto.setLatitude(rider.getCurrentLatitude());
        dto.setStatus(rider.getStatus());

        return dto;
    }

    @Override
    @Transactional
    public void updateRiderLocation(Long riderId, RiderLocationDTO locationDTO) {
        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            throw new RuntimeException("骑手不存在");
        }

        // 更新骑手当前位置
        rider.setCurrentLongitude(locationDTO.getLongitude());
        rider.setCurrentLatitude(locationDTO.getLatitude());
        riderMapper.updateById(rider);

        // 记录位置历史
        RiderLocation location = new RiderLocation();
        location.setRiderId(riderId);
        location.setOrderId(locationDTO.getOrderId());
        location.setLongitude(locationDTO.getLongitude());
        location.setLatitude(locationDTO.getLatitude());
        riderLocationMapper.insert(location);
    }

    @Override
    public List<Rider> getAllRiders() {
        return riderMapper.selectList(null);
    }

    @Override
    @Transactional
    public void createRider(Rider rider) {
        if (rider.getStatus() == null) {
            rider.setStatus(1); // 默认待接单
        }
        riderMapper.insert(rider);
        log.info("创建骑手: {}", rider.getRiderName());
    }
}
