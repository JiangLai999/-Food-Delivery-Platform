package com.fooddelivery.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.dto.RiderLocationDTO;
import com.fooddelivery.entity.*;
import com.fooddelivery.mapper.*;
import com.fooddelivery.service.DeliveryService;
import com.fooddelivery.service.FoodItemService;
import com.fooddelivery.service.WebSocketService;
import com.fooddelivery.dto.RiderLocationDTO;
import com.fooddelivery.entity.Rider;
import com.fooddelivery.mapper.DeliveryTaskMapper;
import com.fooddelivery.mapper.RiderMapper;
import com.fooddelivery.utils.AmapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配送服务实现类
 */
@Slf4j
@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryTaskMapper deliveryTaskMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private RiderMapper riderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private AmapUtil amapUtil;

    @Autowired(required = false)
    private WebSocketService webSocketService;

    @Value("${rider.simulator.speed:30}")
    private Integer riderSpeed;

    @Value("${rider.simulator.total-points:24}")
    private Integer totalPoints;

    private static final Long DEFAULT_RIDER_ID = 1L;

    private final ConcurrentHashMap<Long, Long> lastUpdateTime = new ConcurrentHashMap<>();

    // 配送路径点配置（1分钟配送时间）
    private static final int POINTS_TO_MERCHANT = 2;   // 去商家取餐
    private static final int POINTS_PICKING = 1;       // 取餐等待
    private static final int POINTS_TO_USER = 9;       // 送餐到用户
    
    // 每5秒更新一次位置
    private static final int UPDATE_INTERVAL_SECONDS = 5;

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public DeliveryTask createDeliveryTask(Long orderId, Long riderId) {
        log.info("开始创建配送任务，订单ID: {}, 骑手ID: {}", orderId, riderId);
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在，订单ID: {}", orderId);
            throw new RuntimeException("订单不存在");
        }

        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        if (merchant == null) {
            log.error("商家不存在，商家ID: {}", order.getMerchantId());
            throw new RuntimeException("商家不存在");
        }

        Long actualRiderId = (riderId != null) ? riderId : DEFAULT_RIDER_ID;
        Rider rider = riderMapper.selectById(actualRiderId);
        if (rider == null) {
            rider = riderMapper.selectById(DEFAULT_RIDER_ID);
        }
        if (rider == null) {
            log.error("骑手不存在，骑手ID: {}", actualRiderId);
            throw new RuntimeException("骑手不存在");
        }

log.info("商家坐标: ({}, {}), 骑手原始坐标: ({}, {})", 
            merchant.getLongitude(), merchant.getLatitude(),
            rider.getCurrentLongitude(), rider.getCurrentLatitude());
            
        // 确保商家坐标有效（必须放在使用前）
        BigDecimal merchantLng = merchant.getLongitude();
        BigDecimal merchantLat = merchant.getLatitude();
        if (merchantLng == null || merchantLat == null) {
            merchantLng = new BigDecimal("116.478927");
            merchantLat = new BigDecimal("39.907761");
            log.warn("商家坐标为空，使用默认坐标: {}, {}", merchantLng, merchantLat);
        }
        
        // 设置骑手初始位置在商家附近（随机0.01度范围内）
        double riderStartLng = merchantLng.doubleValue() + (Math.random() - 0.5) * 0.01;
        double riderStartLat = merchantLat.doubleValue() + (Math.random() - 0.5) * 0.01;
        rider.setCurrentLongitude(new BigDecimal(riderStartLng));
        rider.setCurrentLatitude(new BigDecimal(riderStartLat));
        riderMapper.updateById(rider);
        
        log.info("骑手初始位置更新为: ({}, {})", riderStartLng, riderStartLat);

        DeliveryTask task = new DeliveryTask();
        task.setOrderId(orderId);
        task.setMerchantId(order.getMerchantId());
        task.setRiderId(rider.getId());
        
        task.setPickupLongitude(merchantLng);
        task.setPickupLatitude(merchantLat);
        
        // 确保用户坐标有效
        BigDecimal userLng = order.getReceiverLongitude();
        BigDecimal userLat = order.getReceiverLatitude();
        if (userLng == null || userLat == null) {
            userLng = new BigDecimal("116.407400");
            userLat = new BigDecimal("39.904200");
            log.warn("用户坐标为空，使用默认坐标: {}, {}", userLng, userLat);
        }
        task.setDeliveryLongitude(userLng);
        task.setDeliveryLatitude(userLat);
        task.setStatus(0);
        task.setCurrentPosition(0);

        double distanceToMerchant = calculateDistance(
            rider.getCurrentLongitude().doubleValue(),
            rider.getCurrentLatitude().doubleValue(),
            merchantLng.doubleValue(),
            merchantLat.doubleValue()
        );

        double distanceFromMerchantToUser = calculateDistance(
            merchantLng.doubleValue(),
            merchantLat.doubleValue(),
            userLng.doubleValue(),
            userLat.doubleValue()
        );

        double totalDistance = distanceToMerchant + distanceFromMerchantToUser;
        
        List<Map<String, Object>> routePoints = generateDeliveryRoute(
            merchantLng.doubleValue(),
            merchantLat.doubleValue(),
            userLng.doubleValue(),
            userLat.doubleValue()
        );

        task.setRouteData(JSON.toJSONString(routePoints));
        task.setEstimatedTime(3);
        task.setTotalDistance((int) totalDistance);

        deliveryTaskMapper.insert(task);
        log.info("创建配送任务成功，订单ID: {}, 骑手ID: {}, 路线点数: {}, 预计3分钟送达", 
            orderId, rider.getId(), routePoints.size());

        return task;
    }

    private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private List<Map<String, Object>> generateDeliveryRoute(double merchantLng, double merchantLat,
                                                              double userLng, double userLat) {
        List<Map<String, Object>> allPoints = new ArrayList<>();

        try {
            AmapUtil.LatLng merchantPos = new AmapUtil.LatLng(merchantLng, merchantLat);
            AmapUtil.LatLng userPos = new AmapUtil.LatLng(userLng, userLat);

            List<AmapUtil.LatLng> routePoints = amapUtil.planRoute(merchantPos, userPos);

            // 如果路线点太少（少于10个），使用模拟路线
            if (routePoints == null || routePoints.size() < 10) {
                log.warn("高德地图路径点太少({}个)，使用模拟曲线路线", routePoints != null ? routePoints.size() : 0);
                return generateSimulatedRoute(merchantLng, merchantLat, userLng, userLat);
            }

            double ratio = (double) POINTS_TO_USER / routePoints.size();

            for (int i = 0; i < routePoints.size(); i++) {
                AmapUtil.LatLng point = routePoints.get(i);
                int stepNum = (int) (i * ratio);
                
                String phase;
                if (stepNum <= POINTS_TO_MERCHANT) {
                    phase = "going_to_merchant";
                } else if (stepNum <= POINTS_TO_MERCHANT + POINTS_PICKING) {
                    phase = "picking_up";
                } else {
                    phase = "delivering";
                }
                
                double distanceToUser = calculateDistance(point.getLongitude(), point.getLatitude(), userLng, userLat);

                Map<String, Object> pointData = new HashMap<>();
                pointData.put("longitude", point.getLongitude());
                pointData.put("latitude", point.getLatitude());
                pointData.put("phase", phase);
                pointData.put("step", stepNum);
                pointData.put("distanceToUser", (int) distanceToUser);
                allPoints.add(pointData);
            }

            log.info("高德地图路径规划成功，共{}个路径点", allPoints.size());

        } catch (Exception e) {
            log.warn("高德地图路径规划失败，使用模拟曲线路线: {}", e.getMessage());
            return generateSimulatedRoute(merchantLng, merchantLat, userLng, userLat);
        }

        return allPoints;
    }

    private List<Map<String, Object>> generateSimulatedRoute(double merchantLng, double merchantLat,
                                                              double userLng, double userLat) {
        List<Map<String, Object>> steps = new ArrayList<>();

        int totalPoints = POINTS_TO_MERCHANT + POINTS_PICKING + POINTS_TO_USER;
        
        for (int i = 0; i < totalPoints; i++) {
            double t = (double) i / totalPoints;
            
            // 使用直线插值生成更直的路线
            double lng = merchantLng + (userLng - merchantLng) * t;
            double lat = merchantLat + (userLat - merchantLat) * t;
            
            // 添加轻微偏移使路线不那么直
            double offset = Math.sin(t * Math.PI) * 0.001;
            lng += offset;
            lat += offset * 0.5;
            
            String phase;
            if (i < POINTS_TO_MERCHANT) {
                phase = "going_to_merchant";
            } else if (i < POINTS_TO_MERCHANT + POINTS_PICKING) {
                phase = "picking_up";
            } else {
                phase = "delivering";
            }
            
            double distanceToUser = calculateDistance(lng, lat, userLng, userLat);

            Map<String, Object> point = new HashMap<>();
            point.put("longitude", lng);
            point.put("latitude", lat);
            point.put("phase", phase);
            point.put("step", i);
            point.put("distanceToUser", (int) distanceToUser);
            steps.add(point);
        }

        log.info("生成模拟直线路线，从商家到用户，共{}个路径点", steps.size());
        return steps;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void startDelivery(Long orderId) {
        log.info("开始配送，订单ID: {}", orderId);
        
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getOrderId, orderId);
        DeliveryTask task = deliveryTaskMapper.selectOne(wrapper);

        if (task == null) {
            log.error("配送任务不存在，订单ID: {}", orderId);
            throw new RuntimeException("配送任务不存在");
        }
        
        log.info("找到配送任务，任务ID: {}, 当前状态: {}", task.getId(), task.getStatus());

        task.setStatus(1);
        task.setCurrentPosition(0);
        deliveryTaskMapper.updateById(task);

        Order order = orderMapper.selectById(orderId);
        order.setStatus(3);
        orderMapper.updateById(order);
        
        log.info("订单{}状态已更新为配送中", orderId);

        lastUpdateTime.put(orderId, System.currentTimeMillis());
        updateDeliveryPosition(orderId);

        log.info("开始配送订单: {}, 每10秒更新一次位置", orderId);
    }

    @Override
    @Transactional
    public void completeDelivery(Long orderId) {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getOrderId, orderId);
        DeliveryTask task = deliveryTaskMapper.selectOne(wrapper);

        if (task == null) {
            throw new RuntimeException("配送任务不存在");
        }

        // 更新配送任务状态
        task.setStatus(2); // 已送达
        deliveryTaskMapper.updateById(task);

        // 更新订单状态
        Order order = orderMapper.selectById(orderId);
        order.setStatus(4); // 已完成
        orderMapper.updateById(order);

        // 更新骑手状态为待接单
        Rider rider = riderMapper.selectById(task.getRiderId());
        rider.setStatus(1);
        riderMapper.updateById(rider);

        log.info("完成配送订单: {}", orderId);
    }

    @Override
    public DeliveryTask getDeliveryTaskByOrderId(Long orderId) {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getOrderId, orderId);
        return deliveryTaskMapper.selectOne(wrapper);
    }

    @Override
    public void updateRiderLocation(Long orderId, RiderLocationDTO location) {
        DeliveryTask task = getDeliveryTaskByOrderId(orderId);
        if (task == null) {
            throw new RuntimeException("配送任务不存在");
        }
        Rider rider = riderMapper.selectById(task.getRiderId());
        if (location != null && rider != null) {
            if (location.getLongitude() != null) rider.setCurrentLongitude(location.getLongitude());
            if (location.getLatitude() != null) rider.setCurrentLatitude(location.getLatitude());
            if (location.getRiderName() != null) rider.setRiderName(location.getRiderName());
            if (location.getStatus() != null) rider.setStatus(location.getStatus());
            if (location.getEstimatedTime() != null) task.setEstimatedTime(location.getEstimatedTime());
            riderMapper.updateById(rider);
            // 推送位置更新
            RiderLocationDTO dto = new RiderLocationDTO();
            dto.setRiderId(rider.getId());
            dto.setOrderId(orderId);
            dto.setRiderName(rider.getRiderName());
            dto.setLongitude(location.getLongitude());
            dto.setLatitude(location.getLatitude());
            dto.setStatus(rider.getStatus());
            dto.setEstimatedTime(task.getEstimatedTime());
            if (webSocketService != null) {
                webSocketService.sendRiderLocation(orderId, dto);
            }
        }
        // 调用调度器推进路径（如果需要）
        updateDeliveryPosition(orderId);
    }

    @Override
    public com.fooddelivery.dto.RiderLocationDTO getRiderLocation(Long orderId) {
        DeliveryTask task = getDeliveryTaskByOrderId(orderId);
        if (task == null) return null;
        Rider rider = riderMapper.selectById(task.getRiderId());
        if (rider == null) return null;

        RiderLocationDTO dto = new RiderLocationDTO();
        dto.setRiderId(rider.getId());
        dto.setOrderId(orderId);
        dto.setRiderName(rider.getRiderName());
        dto.setRiderPhone(rider.getPhone());
        dto.setLongitude(rider.getCurrentLongitude());
        dto.setLatitude(rider.getCurrentLatitude());
        // 返回配送任务的状态，而不是骑手的状态
        // 配送任务状态：0-待取餐，1-配送中，2-已送达
        dto.setStatus(task.getStatus());
        dto.setEstimatedTime(task.getEstimatedTime());
        dto.setCurrentPosition(task.getCurrentPosition() != null ? task.getCurrentPosition() : 0);

        // 计算当前阶段
        if (task.getRouteData() != null && !task.getRouteData().isEmpty()) {
            try {
                List<Map<String, Object>> steps = (List<Map<String, Object>>) (List<?>) com.alibaba.fastjson2.JSON.parseArray(task.getRouteData(), Map.class);
                if (steps != null && !steps.isEmpty()) {
                    int pos = task.getCurrentPosition() != null ? task.getCurrentPosition() : 0;
                    pos = Math.min(pos, steps.size() - 1);
                    if (pos >= 0 && pos < steps.size()) {
                        Map<String, Object> step = steps.get(pos);
                        String phase = (String) step.get("phase");
                        dto.setPhase(phase);

                        Object distObj = step.get("distanceToUser");
                        if (distObj != null) {
                            dto.setDistanceToUser(((Number) distObj).intValue());
                        }

                        String desc = getStatusDescription(phase, dto.getDistanceToUser());
                        dto.setDescription(desc);
                    }
                }
            } catch (Exception e) {
                log.warn("解析路线数据失败: {}", e.getMessage());
            }
        }

        return dto;
    }

    @Override
    public java.util.List<com.fooddelivery.entity.DeliveryTask> getAllDeliveryTasks() {
        return deliveryTaskMapper.selectList(null);
    }

    @Override
    public java.util.List<com.fooddelivery.entity.DeliveryTask> getMerchantDeliveryTasks(Long merchantId) {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getMerchantId, merchantId);
        wrapper.orderByDesc(com.fooddelivery.entity.DeliveryTask::getCreateTime);
        return deliveryTaskMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void assignRider(Long orderId, Long riderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            throw new RuntimeException("骑手不存在");
        }

        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getOrderId, orderId);
        DeliveryTask task = deliveryTaskMapper.selectOne(wrapper);

        if (task == null) {
            Merchant merchant = merchantMapper.selectById(order.getMerchantId());
            task = new DeliveryTask();
            task.setOrderId(orderId);
            task.setMerchantId(order.getMerchantId());
            task.setRiderId(riderId);
            if (merchant != null) {
                task.setPickupLongitude(merchant.getLongitude());
                task.setPickupLatitude(merchant.getLatitude());
            }
            task.setDeliveryLongitude(order.getReceiverLongitude());
            task.setDeliveryLatitude(order.getReceiverLatitude());
            task.setStatus(0);
            task.setCurrentPosition(0);
            task.setEstimatedTime(30);
            deliveryTaskMapper.insert(task);
        } else {
            task.setRiderId(riderId);
            task.setStatus(0);
            deliveryTaskMapper.updateById(task);
        }

        rider.setStatus(2);
        riderMapper.updateById(rider);

        log.info("分配骑手成功，订单ID: {}, 骑手ID: {}", orderId, riderId);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void scheduledUpdateDeliveryPositions() {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getStatus, 1);
        List<DeliveryTask> activeTasks = deliveryTaskMapper.selectList(wrapper);

        log.info("定时任务检查活跃配送任务，数量: {}", activeTasks.size());
        
        for (DeliveryTask task : activeTasks) {
            try {
                Long lastUpdate = lastUpdateTime.get(task.getOrderId());
                long now = System.currentTimeMillis();
                
                log.info("订单{} 上次更新: {} 当前: {} 间隔: {}ms", 
                    task.getOrderId(), lastUpdate, now, 
                    lastUpdate != null ? (now - lastUpdate) : "null");
                
                if (lastUpdate == null || (now - lastUpdate) >= UPDATE_INTERVAL_SECONDS * 1000L) {
                    log.info("更新订单{}的配送位置", task.getOrderId());
                    updateDeliveryPosition(task.getOrderId());
                    lastUpdateTime.put(task.getOrderId(), now);
                }
            } catch (Exception e) {
                log.error("定时更新配送位置失败，订单ID: {}", task.getOrderId(), e);
            }
        }
    }
    
    /**
     * 异步创建配送任务定时任务
     * 每秒检查是否有状态为2（已接单）但尚未创建配送任务的订单
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void scheduledCreateDeliveryTasks() {
        try {
            // 查找状态为2但没有配送任务的订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getStatus, 2);
            orderWrapper.orderByAsc(Order::getCreateTime);
            orderWrapper.last("LIMIT 10"); // 每次最多处理10个
            List<Order> orders = orderMapper.selectList(orderWrapper);
            
            for (Order order : orders) {
                try {
                    // 检查是否已存在配送任务
                    LambdaQueryWrapper<DeliveryTask> taskWrapper = new LambdaQueryWrapper<>();
                    taskWrapper.eq(DeliveryTask::getOrderId, order.getId());
                    DeliveryTask existingTask = deliveryTaskMapper.selectOne(taskWrapper);
                    
                    if (existingTask == null && order.getRiderId() != null) {
                        log.info("异步创建配送任务，订单ID: {}", order.getId());
                        createDeliveryTask(order.getId(), order.getRiderId());
                        startDelivery(order.getId());
                        log.info("异步创建配送任务成功，订单ID: {}", order.getId());
                        
                        // 增加销量
                        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(OrderItem::getOrderId, order.getId());
                        List<OrderItem> items = orderItemMapper.selectList(wrapper);
                        for (OrderItem item : items) {
                            foodItemService.increaseSalesVolume(item.getFoodId(), item.getQuantity());
                        }
                    }
                } catch (Exception e) {
                    log.error("异步创建配送任务失败，订单ID: {}", order.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("定时任务检查待创建配送任务失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public void updateDeliveryPosition(Long orderId) {
        DeliveryTask task = getDeliveryTaskByOrderId(orderId);
        if (task == null || task.getStatus() != 1) {
            return;
        }

        try {
            List<Map<String, Object>> steps = (List<Map<String, Object>>) (List<?>) JSON.parseArray(task.getRouteData(), Map.class);
            if (steps == null || steps.isEmpty()) {
                return;
            }

            int currentPos = task.getCurrentPosition();
            if (currentPos >= steps.size()) {
                completeDelivery(orderId);
                return;
            }

            Map<String, Object> currentStep = steps.get(currentPos);
            BigDecimal longitude = new BigDecimal(currentStep.get("longitude").toString());
            BigDecimal latitude = new BigDecimal(currentStep.get("latitude").toString());
            String phase = (String) currentStep.get("phase");
            Object distanceObj = currentStep.get("distanceToUser");
            Integer distanceToUser = distanceObj != null ? ((Number) distanceObj).intValue() : 0;

            Rider rider = riderMapper.selectById(task.getRiderId());
            if (rider == null) {
                rider = riderMapper.selectById(DEFAULT_RIDER_ID);
            }
            if (rider == null) return;

            rider.setCurrentLongitude(longitude);
            rider.setCurrentLatitude(latitude);
            riderMapper.updateById(rider);

        int remainingSteps = steps.size() - currentPos - 1;
        int estimatedSeconds = remainingSteps * UPDATE_INTERVAL_SECONDS;
        task.setCurrentPosition(currentPos + 1);
        task.setEstimatedTime(Math.max(0, (estimatedSeconds + 59) / 60));
        deliveryTaskMapper.updateById(task);

        String statusDescription = getStatusDescription(phase, distanceToUser);
        
        log.info("订单{}配送进度: 位置{}/{}, 阶段: {}, 距离用户: {}米, 剩余时间: {}分钟",
            orderId, currentPos + 1, steps.size(), phase, distanceToUser, task.getEstimatedTime());

        if (webSocketService != null) {
            RiderLocationDTO locationDTO = new RiderLocationDTO();
            locationDTO.setRiderId(rider.getId());
            locationDTO.setOrderId(orderId);
            locationDTO.setRiderName(rider.getRiderName());
            locationDTO.setLongitude(longitude);
            locationDTO.setLatitude(latitude);
            locationDTO.setStatus(rider.getStatus());
            locationDTO.setEstimatedTime(task.getEstimatedTime());
            locationDTO.setPhase(phase);
            locationDTO.setDistanceToUser(distanceToUser);
            locationDTO.setDescription(statusDescription);
            locationDTO.setCurrentPosition(task.getCurrentPosition());

            webSocketService.sendRiderLocation(orderId, locationDTO);
            }

            log.debug("更新配送位置，订单ID: {}, 位置: {}/{}, 阶段: {}, 距离用户: {}米, 状态: {}", 
                orderId, currentPos + 1, steps.size(), phase, distanceToUser, statusDescription);

        } catch (Exception e) {
            log.error("更新配送位置失败", e);
        }
    }

    private String getStatusDescription(String phase, Integer distanceToUser) {
        if (phase == null) return "配送中";
        
        switch (phase) {
            case "going_to_merchant":
                return "前往商家中";
            case "picking_up":
                return "正在取餐";
            case "delivering":
                if (distanceToUser != null && distanceToUser > 0) {
                    return "距离用户 " + distanceToUser + " 米";
                }
                return "配送中";
            case "arrived":
                return "配送完成";
            default:
                return "配送中";
        }
    }
}
