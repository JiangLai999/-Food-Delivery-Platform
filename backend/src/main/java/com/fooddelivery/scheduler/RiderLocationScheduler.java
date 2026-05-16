package com.fooddelivery.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.entity.DeliveryTask;
import com.fooddelivery.mapper.DeliveryTaskMapper;
import com.fooddelivery.service.DeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 骑手位置更新定时任务
 */
@Slf4j
@Component
public class RiderLocationScheduler {

    @Autowired
    private DeliveryTaskMapper deliveryTaskMapper;

    @Autowired
    private DeliveryService deliveryService;

    @Value("${rider.simulator.enabled:true}")
    private Boolean simulatorEnabled;

    /**
     * 定时更新所有配送中订单的骑手位置
     * 每5秒执行一次，2分钟内(24个位置点)完成配送
     */
    @Scheduled(fixedDelayString = "${rider.simulator.update-interval:5000}")
    public void updateRiderLocations() {
        if (!simulatorEnabled) {
            return;
        }

        try {
            // 查询所有配送中的任务
            LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeliveryTask::getStatus, 1); // 配送中
            List<DeliveryTask> tasks = deliveryTaskMapper.selectList(wrapper);

            if (tasks.isEmpty()) {
                return;
            }

            log.debug("开始更新骑手位置，配送中订单数: {}", tasks.size());

            for (DeliveryTask task : tasks) {
                try {
                    deliveryService.updateDeliveryPosition(task.getOrderId());
                } catch (Exception e) {
                    log.error("更新订单 {} 的骑手位置失败", task.getOrderId(), e);
                }
            }

        } catch (Exception e) {
            log.error("骑手位置更新定时任务执行失败", e);
        }
    }
}
