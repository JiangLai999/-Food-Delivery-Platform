package com.fooddelivery.service;

import com.fooddelivery.dto.RiderLocationDTO;

/**
 * WebSocket 服务，用于推送实时消息（如骑手位置）
 */
public interface WebSocketService {
    void sendRiderLocation(Long orderId, RiderLocationDTO location);
}
