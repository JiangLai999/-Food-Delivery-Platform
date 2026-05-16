package com.fooddelivery.websocket;

import com.alibaba.fastjson2.JSON;
import com.fooddelivery.dto.RiderLocationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 原生WebSocket服务器 - 供Android客户端使用
 * 与STOMP WebSocket共存，处理原生WebSocket连接
 */
@Slf4j
@Component
@ServerEndpoint("/native-ws/{userId}")
@SuppressWarnings("unchecked")
public class NativeWebSocketServer {

    // 存储所有连接（按用户ID）
    private static final Map<Long, Session> userSessions = new ConcurrentHashMap<>();
    
    // 存储用户订阅的订单（用户ID -> 订单ID）
    private static final Map<Long, Long> userOrderMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功时触发
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        log.info("WebSocket连接建立: userIdStr = {}", userIdStr);
        log.info("当前在线会话数: {}", userSessions.size());
        log.info("所有会话key: {}", userSessions.keySet());
        
        // 处理管理员连接 (格式: admin_123)
        if (userIdStr != null && userIdStr.startsWith("admin_")) {
            String adminIdStr = userIdStr.substring(6);
            try {
                Long adminId = Long.parseLong(adminIdStr);
                Long sessionKey = -1000L - adminId;
                userSessions.put(sessionKey, session);
                log.info("管理员WebSocket连接建立: 管理员ID = {}, sessionKey = {}", adminId, sessionKey);
                sendMessage(session, createMessage("CONNECTED", "管理员连接成功", null));
                return;
            } catch (NumberFormatException e) {
                log.error("管理员ID解析失败: {}", userIdStr);
            }
        }
        
        // 处理商家连接 (格式: merchant_123)
        if (userIdStr != null && userIdStr.startsWith("merchant_")) {
            String merchantIdStr = userIdStr.substring(9);
            try {
                Long merchantId = Long.parseLong(merchantIdStr);
                Long sessionKey = -2000L - merchantId;
                userSessions.put(sessionKey, session);  // 使用负数区分商家
                log.info("商家WebSocket连接建立: 商家ID = {}, sessionKey = {}", merchantId, sessionKey);
                sendMessage(session, createMessage("CONNECTED", "商家连接成功", null));
                return;
            } catch (NumberFormatException e) {
                log.error("商家ID解析失败: {}", userIdStr);
            }
        }
        
        // 普通用户连接
        try {
            Long userId = Long.parseLong(userIdStr);
            userSessions.put(userId, session);
            log.info("用户WebSocket连接建立: 用户ID = {}", userId);
        } catch (NumberFormatException e) {
            log.error("用户ID解析失败: {}", userIdStr);
            return;
        }
        
        // 发送连接成功消息
        sendMessage(session, createMessage("CONNECTED", "连接成功", null));
    }

    /**
     * 连接关闭时触发
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userIdStr) {
        log.info("WebSocket连接关闭: userIdStr = {}", userIdStr);
        
        // 处理管理员连接
        if (userIdStr != null && userIdStr.startsWith("admin_")) {
            String adminIdStr = userIdStr.substring(6);
            try {
                Long adminId = Long.parseLong(adminIdStr);
                userSessions.remove(-1000L - adminId);
                return;
            } catch (NumberFormatException e) {
                // continue
            }
        }
        
        // 处理商家连接
        if (userIdStr != null && userIdStr.startsWith("merchant_")) {
            String merchantIdStr = userIdStr.substring(9);
            try {
                Long merchantId = Long.parseLong(merchantIdStr);
                userSessions.remove(-2000L - merchantId);
                return;
            } catch (NumberFormatException e) {
                // continue
            }
        }
        
        // 普通用户
        try {
            Long userId = Long.parseLong(userIdStr);
            userSessions.remove(userId);
            userOrderMap.remove(userId);
        } catch (NumberFormatException e) {
            log.error("用户ID解析失败: {}", userIdStr);
        }
    }

    /**
     * 收到客户端消息时触发
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("收到消息: 用户ID = {}, 消息 = {}", userId, message);
        
        try {
            Map<String, Object> msg = JSON.parseObject(message, Map.class);
            String type = (String) msg.get("type");
            
            if ("SUBSCRIBE_ORDER".equals(type)) {
                // 订阅订单
                Long orderId = Long.valueOf(msg.get("orderId").toString());
                userOrderMap.put(userId, orderId);
                log.info("用户 {} 订阅订单 {}", userId, orderId);
                sendMessage(session, createMessage("SUBSCRIBED", "订阅成功", orderId));
            } else if ("UNSUBSCRIBE_ORDER".equals(type)) {
                // 取消订阅
                userOrderMap.remove(userId);
                sendMessage(session, createMessage("UNSUBSCRIBED", "取消订阅成功", null));
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
            sendMessage(session, createMessage("ERROR", "消息格式错误", null));
        }
    }

    /**
     * 发生错误时触发
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        log.error("WebSocket错误: 用户ID = {}", userId, error);
        userSessions.remove(userId);
        userOrderMap.remove(userId);
    }

    /**
     * 向指定用户发送消息
     */
    public static void sendMessageToUser(Long userId, String message) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    /**
     * 向指定订单的所有订阅者发送骑手位置更新
     */
    public static void sendRiderLocationToOrder(Long orderId, RiderLocationDTO location) {
        String message = createRiderLocationMessage(orderId, location);
        
        // 找到所有订阅了该订单的用户
        userOrderMap.forEach((userId, subOrderId) -> {
            if (subOrderId.equals(orderId)) {
                Session session = userSessions.get(userId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                    log.debug("向用户 {} 发送骑手位置更新", userId);
                }
            }
        });
    }

    /**
     * 向指定订单的所有订阅者发送订单状态更新
     */
    public static void sendOrderStatusToOrder(Long orderId, Integer status, String statusText) {
        String message = createOrderStatusMessage(orderId, status, statusText);
        
        userOrderMap.forEach((userId, subOrderId) -> {
            if (subOrderId.equals(orderId)) {
                Session session = userSessions.get(userId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                    log.debug("向用户 {} 发送订单状态更新", userId);
                }
            }
        });
    }

    /**
     * 向指定用户发送聊天消息
     */
    public static void sendChatMessage(Long userId, Map<String, Object> messageData) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            Map<String, Object> msg = new java.util.HashMap<>();
            msg.put("type", "CHAT_MESSAGE");
            msg.put("data", messageData);
            msg.put("timestamp", System.currentTimeMillis());
            sendMessage(session, JSON.toJSONString(msg));
            log.debug("向用户 {} 发送聊天消息", userId);
        } else {
            log.debug("用户 {} 不在线，消息未推送", userId);
        }
    }

    /**
     * 向管理员发送聊天消息
     */
    public static void sendChatMessageToAdmin(Long adminId, Map<String, Object> messageData) {
        // 使用与连接时相同的key
        Long sessionKey = -1000L - adminId;
        Session session = userSessions.get(sessionKey);
        if (session != null && session.isOpen()) {
            Map<String, Object> msg = new java.util.HashMap<>();
            msg.put("type", "CHAT_MESSAGE");
            msg.put("data", messageData);
            msg.put("timestamp", System.currentTimeMillis());
            sendMessage(session, JSON.toJSONString(msg));
            log.info("向管理员 {} 发送聊天消息: adminId={}, sessionKey={}", adminId, adminId, sessionKey);
        } else {
            log.warn("管理员 {} 不在线，消息未推送。sessionKey={}, 当前会话数={}", adminId, sessionKey, userSessions.size());
            log.warn("当前所有会话的key: {}", userSessions.keySet());
        }
    }

    /**
     * 向指定商家/客服发送聊天消息（用于用户发消息给商家）
     */
    public static void sendChatMessageToMerchant(Long merchantId, Map<String, Object> messageData) {
        // 使用与连接时相同的key (-2000L - merchantId)
        Long sessionKey = -2000L - merchantId;
        Session session = userSessions.get(sessionKey);
        if (session != null && session.isOpen()) {
            Map<String, Object> msg = new java.util.HashMap<>();
            msg.put("type", "CHAT_MESSAGE");
            msg.put("data", messageData);
            msg.put("timestamp", System.currentTimeMillis());
            sendMessage(session, JSON.toJSONString(msg));
            log.debug("向商家 {} 发送聊天消息", merchantId);
        } else {
            log.debug("商家 {} 不在线，消息未推送", merchantId);
        }
    }

    /**
     * 广播消息到所有在线用户（用于系统公告）
     */
    public static void broadcastMessage(String type, Map<String, Object> data) {
        Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("type", type);
        msg.put("data", data);
        msg.put("timestamp", System.currentTimeMillis());
        
        String message = JSON.toJSONString(msg);
        
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * 发送消息
     */
    private static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    /**
     * 创建消息
     */
    private static String createMessage(String type, String message, Long orderId) {
        Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("type", type);
        msg.put("message", message);
        msg.put("timestamp", System.currentTimeMillis());
        if (orderId != null) {
            msg.put("orderId", orderId);
        }
        return JSON.toJSONString(msg);
    }

    /**
     * 创建骑手位置消息
     */
    private static String createRiderLocationMessage(Long orderId, RiderLocationDTO location) {
        Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("type", "RIDER_LOCATION");
        msg.put("orderId", orderId);
        msg.put("data", location);
        msg.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(msg);
    }

    /**
     * 创建订单状态消息
     */
    private static String createOrderStatusMessage(Long orderId, Integer status, String statusText) {
        Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("type", "ORDER_STATUS");
        msg.put("orderId", orderId);
        msg.put("status", status);
        msg.put("statusText", statusText);
        msg.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(msg);
    }
}
