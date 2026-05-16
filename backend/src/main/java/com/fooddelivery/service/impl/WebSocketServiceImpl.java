package com.fooddelivery.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fooddelivery.dto.RiderLocationDTO;
import com.fooddelivery.service.WebSocketService;
import com.fooddelivery.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Override
    public void sendRiderLocation(Long orderId, RiderLocationDTO location) {
        if (location == null || orderId == null) return;
        String payload = JSON.toJSONString(location);
        try {
            WebSocketServer.sendMessageToOrder(orderId, payload);
            log.info("推送骑手位置信息，订单 {}: {}", orderId, payload);
        } catch (Exception e) {
            log.error("发送骑手位置信息失败，订单 {}", orderId, e);
        }
    }
}
