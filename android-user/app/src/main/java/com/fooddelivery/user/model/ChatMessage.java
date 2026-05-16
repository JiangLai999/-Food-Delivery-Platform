package com.fooddelivery.user.model;

/**
 * 聊天消息模型
 */
public class ChatMessage {
    
    // 消息类型
    public static final int TYPE_USER = 1;      // 用户发送
    public static final int TYPE_MERCHANT = 2;  // 商家回复
    public static final int TYPE_ADMIN = 3;      // 客服/管理员
    
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Integer fromUserType;  // 1-用户，2-商家，3-客服/管理员
    private Integer toUserType;
    private String content;
    private Integer contentType; // 0-文本，1-图片
    private Integer isRead;
    private String createTime;
    
    public ChatMessage() {
    }
    
    public ChatMessage(String content, Integer fromUserType) {
        this.content = content;
        this.fromUserType = fromUserType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getFromUserId() {
        return fromUserId;
    }
    
    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }
    
    public Long getToUserId() {
        return toUserId;
    }
    
    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }
    
    public Integer getFromUserType() {
        return fromUserType;
    }
    
    public void setFromUserType(Integer fromUserType) {
        this.fromUserType = fromUserType;
    }
    
    public Integer getToUserType() {
        return toUserType;
    }
    
    public void setToUserType(Integer toUserType) {
        this.toUserType = toUserType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getContentType() {
        return contentType;
    }
    
    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }
    
    public Integer getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
    
    public String getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
