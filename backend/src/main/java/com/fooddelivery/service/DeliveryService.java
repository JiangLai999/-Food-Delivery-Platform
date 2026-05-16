    package com.fooddelivery.service;

    import com.fooddelivery.entity.DeliveryTask;

    /**
     * 配送服务接口
     */
    public interface DeliveryService {

        /**
         * 创建配送任务
         */
        DeliveryTask createDeliveryTask(Long orderId, Long riderId);

        /**
         * 开始配送
         */
        void startDelivery(Long orderId);

        /**
         * 完成配送
         */
        void completeDelivery(Long orderId);

        /**
         * 获取订单配送任务
         */
        DeliveryTask getDeliveryTaskByOrderId(Long orderId);

        /**
         * Update rider location for an ongoing delivery
         */
        void updateRiderLocation(Long orderId, com.fooddelivery.dto.RiderLocationDTO location);

        /**
         * Get current rider location for an order
         */
        com.fooddelivery.dto.RiderLocationDTO getRiderLocation(Long orderId);

        /**
         * Admin: fetch all delivery tasks (for oversight)
         */
        java.util.List<com.fooddelivery.entity.DeliveryTask> getAllDeliveryTasks();

        /**
         * 获取商家配送任务列表
         */
        java.util.List<com.fooddelivery.entity.DeliveryTask> getMerchantDeliveryTasks(Long merchantId);

        /**
         * 分配骑手
         */
        void assignRider(Long orderId, Long riderId);

        /**
         * 更新配送任务位置
         */
        void updateDeliveryPosition(Long orderId);
    }
