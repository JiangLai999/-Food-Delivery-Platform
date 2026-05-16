package com.fooddelivery.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.entity.RiderLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务 - 使用STOMP协议推送消息
 */
@Slf4j
@Component
public class WebSocketServer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static WebSocketServer instance;
    
    @PostConstruct
    public void init() {
        instance = this;
        log.info("WebSocketServer initialized");
    }
    
    public static WebSocketServer getInstance() {
        return instance;
    }

    /**
     * 发送消息到指定订单频道
     */
    public static void sendMessageToOrder(Long orderId, String payload) {
        if (orderId == null || payload == null) return;
        try {
            if (instance != null && instance.messagingTemplate != null) {
                String destination = "/topic/order/" + orderId;
                instance.messagingTemplate.convertAndSend(destination, payload);
                log.info("[WebSocket] Push to order {}: {}", orderId, payload);
            } else {
                log.warn("[WebSocket] MessagingTemplate not available, message not sent");
            }
        } catch (Exception e) {
            log.error("[WebSocket] Failed to send message to order {}: {}", orderId, e.getMessage());
        }
    }

    /**
     * 推送骑手位置更新
     */
    public static void sendRiderLocation(Long orderId, RiderLocation location) {
        if (orderId == null || location == null) return;
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "RIDER_LOCATION");
            message.put("orderId", orderId);
            message.put("data", location);
            message.put("timestamp", System.currentTimeMillis());
            
            String payload = instance.objectMapper.writeValueAsString(message);
            sendMessageToOrder(orderId, payload);
            
            // 同时推送到原生WebSocket（供Android端使用）
            com.fooddelivery.dto.RiderLocationDTO dto = new com.fooddelivery.dto.RiderLocationDTO();
            dto.setRiderId(location.getRiderId());
            dto.setOrderId(orderId);
            dto.setLongitude(location.getLongitude());
            dto.setLatitude(location.getLatitude());
            NativeWebSocketServer.sendRiderLocationToOrder(orderId, dto);
        } catch (Exception e) {
            log.error("[WebSocket] Failed to send rider location: {}", e.getMessage());
        }
    }

    /**
     * 推送订单状态更新
     */
    public static void sendOrderStatusUpdate(Long orderId, Integer status, String statusText) {
        if (orderId == null) return;
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ORDER_STATUS");
            message.put("orderId", orderId);
            message.put("status", status);
            message.put("statusText", statusText);
            message.put("timestamp", System.currentTimeMillis());
            
            String payload = instance.objectMapper.writeValueAsString(message);
            sendMessageToOrder(orderId, payload);
            
            // 同时推送到原生WebSocket（供Android端使用）
            NativeWebSocketServer.sendOrderStatusToOrder(orderId, status, statusText);
        } catch (Exception e) {
            log.error("[WebSocket] Failed to send order status: {}", e.getMessage());
        }
    }

    /**
     * 推送系统消息
     */
    public static void sendSystemMessage(Long userId, String title, String content) {
        if (userId == null) return;
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "SYSTEM_MESSAGE");
            message.put("title", title);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());
            
            String payload = instance.objectMapper.writeValueAsString(message);
            String destination = "/topic/user/" + userId;
            
            if (instance != null && instance.messagingTemplate != null) {
                instance.messagingTemplate.convertAndSend(destination, payload);
                log.info("[WebSocket] System message sent to user {}", userId);
            }
        } catch (Exception e) {
            log.error("[WebSocket] Failed to send system message: {}", e.getMessage());
        }
    }

    /**
     * 订阅订单（记录日志）
     */
    public static void subscribeOrder(Long orderId, Long riderId) {
        log.info("[WebSocket] Subscribe order {} to rider {}", orderId, riderId);
    }

    /**
     * 实例方法 - 发送消息到指定目的地
     */
    public void sendToDestination(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.info("[WebSocket] Message sent to {}", destination);
        } catch (Exception e) {
            log.error("[WebSocket] Failed to send message: {}", e.getMessage());
        }
    }
}
