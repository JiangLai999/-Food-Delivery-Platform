package com.fooddelivery.controller;

import com.fooddelivery.dto.ChatMessageDTO;
import com.fooddelivery.entity.ChatMessage;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.entity.User;
import com.fooddelivery.mapper.ChatMessageMapper;
import com.fooddelivery.mapper.MerchantMapper;
import com.fooddelivery.mapper.UserMapper;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import com.fooddelivery.websocket.NativeWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @PostMapping("/send")
    public Result<String> sendMessage(@RequestHeader("Authorization") String token,
                                      @Valid @RequestBody ChatMessageDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);
        
        ChatMessage message = new ChatMessage();
        message.setFromUserId(userId);
        // userType: "user"=1, "merchant"=2, "admin"=3
        if ("merchant".equals(userType)) {
            message.setFromUserType(2);
        } else if ("admin".equals(userType)) {
            message.setFromUserType(3);
        } else {
            message.setFromUserType(1);
        }
        message.setToUserId(dto.getToUserId());
        message.setToUserType(dto.getToUserType());
        message.setContent(dto.getContent());
        message.setContentType(dto.getContentType());

        chatMessageMapper.insert(message);

        log.info("发送消息: from={}, type={}, to={}, type={}, content={}",
            userId, message.getFromUserType(), dto.getToUserId(), dto.getToUserType(), dto.getContent());

        // 通过WebSocket推送消息给接收方
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("id", message.getId());
        messageData.put("fromUserId", userId);
        messageData.put("fromUserType", message.getFromUserType());
        messageData.put("toUserId", dto.getToUserId());
        messageData.put("toUserType", dto.getToUserType());
        messageData.put("content", dto.getContent());
        messageData.put("contentType", dto.getContentType());
        messageData.put("timestamp", message.getCreateTime());

        // 根据接收方类型发送消息
        Integer toUserType = dto.getToUserType();
        log.info("准备发送消息: fromUserType={}, fromUserId={}, toUserType={}, toUserId={}",
            message.getFromUserType(), userId, toUserType, dto.getToUserId());
        
        if (toUserType != null && toUserType == 2) {
            // 接收方是商家
            NativeWebSocketServer.sendChatMessageToMerchant(dto.getToUserId(), messageData);
            log.info("用户/管理员 {} 发送消息给商家 {}", userId, dto.getToUserId());
        } else if (toUserType != null && toUserType == 3) {
            // 接收方是管理员
            log.info("尝试向管理员 {} 发送消息", dto.getToUserId());
            NativeWebSocketServer.sendChatMessageToAdmin(dto.getToUserId(), messageData);
            log.info("用户/商家 {} 发送消息给管理员 {}", userId, dto.getToUserId());
        } else {
            // 接收方是用户
            NativeWebSocketServer.sendChatMessage(dto.getToUserId(), messageData);
            log.info("用户/管理员 {} 发送消息给用户 {}", userId, dto.getToUserId());
        }
        
        return Result.success("消息发送成功");
    }

    @GetMapping("/history/{withUserId}")
    public Result<List<ChatMessage>> getHistory(@RequestHeader("Authorization") String token,
                                                @PathVariable Long withUserId,
                                                @RequestParam(required = false) Integer withUserType) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);

        List<ChatMessage> list = new ArrayList<>();
        
        if ("merchant".equals(userType)) {
            // 商家查询与用户的聊天记录（userType=1），排除与管理员的聊天
            // 必须确保是商家ID和用户ID之间的直接对话
            list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getFromUserId, userId)
                    .eq(ChatMessage::getFromUserType, 2)  // 商家发送
                    .eq(ChatMessage::getToUserId, withUserId)
                    .eq(ChatMessage::getToUserType, 1)));  // 发给用户
            list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getFromUserId, withUserId)
                    .eq(ChatMessage::getFromUserType, 1)  // 用户发送
                    .eq(ChatMessage::getToUserId, userId)
                    .eq(ChatMessage::getToUserType, 2)));  // 发给商家
            
            log.info("商家 {} 查询与用户 {} 的聊天历史，查询到 {} 条消息", userId, withUserId, list.size());
        } else if ("admin".equals(userType)) {
            // 管理员查询与用户/商家的聊天
            if (withUserType != null && withUserType == 1) {
                // 与用户的聊天
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, userId)
                        .eq(ChatMessage::getToUserId, withUserId)
                        .eq(ChatMessage::getToUserType, 1)));
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, withUserId)
                        .eq(ChatMessage::getFromUserType, 1)
                        .eq(ChatMessage::getToUserId, userId)
                        .eq(ChatMessage::getToUserType, 3)));
            } else if (withUserType != null && withUserType == 2) {
                // 与商家的聊天
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, userId)
                        .eq(ChatMessage::getToUserId, withUserId)
                        .eq(ChatMessage::getToUserType, 2)));
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, withUserId)
                        .eq(ChatMessage::getFromUserType, 2)
                        .eq(ChatMessage::getToUserId, userId)
                        .eq(ChatMessage::getToUserType, 3)));
            }
        } else {
            // 普通用户查询
            if (withUserType != null) {
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, userId)
                        .eq(ChatMessage::getToUserId, withUserId)
                        .eq(ChatMessage::getToUserType, withUserType)));
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, withUserId)
                        .eq(ChatMessage::getFromUserType, withUserType)
                        .eq(ChatMessage::getToUserId, userId)));
            } else {
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getToUserId, withUserId)));
                list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getFromUserId, withUserId).eq(ChatMessage::getToUserId, userId)));
            }
        }

        list.sort(Comparator.comparing(ChatMessage::getCreateTime));
        return Result.success(list);
    }

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(@RequestHeader("Authorization") String token) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        
        log.info("商家 {} 开始获取会话列表", merchantId);
        
        // 只获取与用户（userType=1）有聊天记录的所有用户，同时确保消息是发给商家的（toUserType=2）
        // 严格排除与管理员(userType=3)的聊天
        List<ChatMessage> messages = chatMessageMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                .and(wrapper -> wrapper
                    // 情况1：用户发给商家的消息 (fromUserType=1, toUserId=merchantId, toUserType=2)
                    .and(w -> w.eq(ChatMessage::getFromUserType, 1)
                              .eq(ChatMessage::getToUserId, merchantId)
                              .eq(ChatMessage::getToUserType, 2))
                    // 情况2：商家发给用户的消息 (fromUserId=merchantId, fromUserType=2, toUserType=1)
                    .or(w -> w.eq(ChatMessage::getFromUserId, merchantId)
                              .eq(ChatMessage::getFromUserType, 2)
                              .eq(ChatMessage::getToUserType, 1))
                )
                .orderByDesc(ChatMessage::getCreateTime)
        );
        
        log.info("商家 {} 获取会话列表，查询到 {} 条消息", merchantId, messages.size());
        
        // 打印所有消息的详情用于调试
        for (ChatMessage msg : messages) {
            log.info("消息详情: fromUserId={}, fromUserType={}, toUserId={}, toUserType={}, content={}", 
                msg.getFromUserId(), msg.getFromUserType(), msg.getToUserId(), msg.getToUserType(), msg.getContent());
        }
        
        // 按用户ID分组，获取每个用户的最后一条消息
        Map<Long, Map<String, Object>> conversationMap = new LinkedHashMap<>();
        for (ChatMessage msg : messages) {
            // 获取对方的用户ID和类型 - 先判断消息来源类型，而不是ID
            Long otherUserId;
            Integer otherUserType;
            
            if (msg.getFromUserType() == 2) {
                // 商家发送的消息，对方是接收方
                otherUserId = msg.getToUserId();
                otherUserType = msg.getToUserType();
            } else {
                // 用户发送的消息（fromUserType=1），对方是发送方
                otherUserId = msg.getFromUserId();
                otherUserType = msg.getFromUserType();
            }
            
            log.info("处理会话: otherUserId={}, otherUserType={}", otherUserId, otherUserType);
            
            // 跳过非用户类型的会话（排除管理员和商家自己）
            if (otherUserType == null || otherUserType != 1) {
                log.warn("商家 {} 跳过非用户会话: otherUserId={}, otherUserType={}", merchantId, otherUserId, otherUserType);
                continue;
            }
            
            if (!conversationMap.containsKey(otherUserId)) {
                Map<String, Object> conv = new HashMap<>();
                conv.put("userId", otherUserId);
                conv.put("userType", otherUserType);  // 正确的用户类型
                conv.put("lastMessage", msg.getContent());
                conv.put("lastTime", msg.getCreateTime());
                conv.put("unreadCount", 0);
                
                // 获取用户信息
                User user = userMapper.selectById(otherUserId);
                if (user != null) {
                    conv.put("userName", user.getNickname() != null ? user.getNickname() : user.getPhone());
                    conv.put("userAvatar", user.getAvatar());
                    log.info("找到用户: userId={}, userName={}", otherUserId, user.getNickname());
                } else {
                    log.warn("商家 {} 获取用户信息失败: userId={}", merchantId, otherUserId);
                }
                
                conversationMap.put(otherUserId, conv);
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>(conversationMap.values());
        log.info("商家 {} 获取到 {} 个用户会话", merchantId, result.size());
        
        return Result.success(result);
    }

    @GetMapping("/admin/conversations")
    public Result<List<Map<String, Object>>> getAdminConversations(@RequestHeader("Authorization") String token) {
        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!"admin".equals(userType)) {
            return Result.error("无权限");
        }

        // 只获取与管理员相关的聊天记录：
        // 1. 用户发给管理员的消息 (fromUserType=1, toUserType=3)
        // 2. 商家发给管理员的消息 (fromUserType=2, toUserType=3)
        // 3. 管理员发给用户的消息 (fromUserType=3, toUserType=1)
        // 4. 管理员发给商家的消息 (fromUserType=3, toUserType=2)
        // 排除商家与用户之间的聊天 (fromUserType=1/2, toUserType=2/1)
        List<ChatMessage> messages = chatMessageMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                .and(wrapper -> wrapper
                    // 情况1：用户发给管理员
                    .and(w -> w.eq(ChatMessage::getFromUserType, 1)
                              .eq(ChatMessage::getToUserType, 3))
                    // 情况2：商家发给管理员
                    .or(w -> w.eq(ChatMessage::getFromUserType, 2)
                              .eq(ChatMessage::getToUserType, 3))
                    // 情况3：管理员发给用户
                    .or(w -> w.eq(ChatMessage::getFromUserType, 3)
                              .eq(ChatMessage::getToUserType, 1))
                    // 情况4：管理员发给商家
                    .or(w -> w.eq(ChatMessage::getFromUserType, 3)
                              .eq(ChatMessage::getToUserType, 2))
                )
                .isNotNull(ChatMessage::getContent)
                .ne(ChatMessage::getContent, "")
                .orderByDesc(ChatMessage::getCreateTime)
        );

        // 按用户ID和用户类型分组，只保留最新的会话
        Map<String, Map<String, Object>> conversationMap = new LinkedHashMap<>();
        for (ChatMessage msg : messages) {
            // 确定对方是谁（排除管理员自己）
            Long otherUserId;
            Integer otherUserType;
            
            if (msg.getFromUserType() == 3) {
                // 消息来自管理员，对方是接收方
                otherUserId = msg.getToUserId();
                otherUserType = msg.getToUserType();
            } else {
                // 消息来自用户或商家
                otherUserId = msg.getFromUserId();
                otherUserType = msg.getFromUserType();
            }
            
            // 跳过无效的对方
            if (otherUserId == null || otherUserType == null) continue;
            // 跳过管理员自己
            if (otherUserType == 3) continue;
            
            String key = otherUserType + "_" + otherUserId;
            
            // 只添加第一条消息（即最新的）
            if (!conversationMap.containsKey(key)) {
                Map<String, Object> conv = new HashMap<>();
                conv.put("userId", otherUserId);
                conv.put("userType", otherUserType);
                conv.put("lastMessage", msg.getContent());
                conv.put("lastTime", msg.getCreateTime());
                conv.put("unreadCount", 0);
                conversationMap.put(key, conv);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> conv : conversationMap.values()) {
            Integer userTypeVal = (Integer) conv.get("userType");
            Long userIdVal = (Long) conv.get("userId");
            if (userTypeVal == 1) {
                User user = userMapper.selectById(userIdVal);
                if (user != null) {
                    conv.put("userName", user.getNickname() != null && !user.getNickname().isEmpty() 
                        ? user.getNickname() : user.getPhone());
                    conv.put("userAvatar", user.getAvatar());
                }
            } else if (userTypeVal == 2) {
                Merchant merchant = merchantMapper.selectById(userIdVal);
                if (merchant != null) {
                    conv.put("userName", merchant.getMerchantName());
                    conv.put("userAvatar", merchant.getLogo());
                }
            }
            result.add(conv);
        }
        
        result.sort((a, b) -> {
            Object timeA = a.get("lastTime");
            Object timeB = b.get("lastTime");
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return ((java.time.LocalDateTime) timeB).compareTo((java.time.LocalDateTime) timeA);
        });
        
        return Result.success(result);
    }

    @GetMapping("/admin/history")
    public Result<List<ChatMessage>> getAdminHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam Long userId,
            @RequestParam Integer userType) {
        String tokenUserType = jwtUtil.getUserTypeFromToken(token);
        if (!"admin".equals(tokenUserType)) {
            return Result.error("无权限");
        }

        List<ChatMessage> list = new ArrayList<>();
        list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getFromUserType, userType)
                .eq(ChatMessage::getToUserType, 3)));
        list.addAll(chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getToUserId, userId).eq(ChatMessage::getToUserType, userType)
                .eq(ChatMessage::getFromUserType, 3)));

        list.sort(Comparator.comparing(ChatMessage::getCreateTime));
        return Result.success(list);
    }

    @DeleteMapping("/admin/conversation")
    public Result<String> deleteConversation(
            @RequestHeader("Authorization") String token,
            @RequestParam("userId") Long userId,
            @RequestParam("userType") Integer userType) {
        String tokenUserType = jwtUtil.getUserTypeFromToken(token);
        if (!"admin".equals(tokenUserType)) {
            return Result.error("无权限");
        }

        // 删除管理员与该用户的所有聊天记录
        chatMessageMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getFromUserId, userId).eq(ChatMessage::getFromUserType, userType)
                .eq(ChatMessage::getToUserType, 3));
        chatMessageMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getToUserId, userId).eq(ChatMessage::getToUserType, userType)
                .eq(ChatMessage::getFromUserType, 3));

        return Result.success("删除成功");
    }
}
