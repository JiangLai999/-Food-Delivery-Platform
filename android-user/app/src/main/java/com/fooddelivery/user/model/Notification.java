package com.fooddelivery.user.model;

import java.io.Serializable;

/**
 * 消息通知模型
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String title;           // 消息标题
    private String content;         // 消息内容
    private String type;            // 类型：ORDER-订单, PROMOTION-优惠, SYSTEM-系统
    private String createTime;      // 创建时间
    private boolean isRead;         // 是否已读
    private Long relatedId;         // 关联ID（如订单ID）
    private String imageUrl;        // 图片URL
    
    public Notification() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public Long getRelatedId() {
        return relatedId;
    }
    
    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    /**
     * 获取类型文本
     */
    public String getTypeText() {
        switch (type) {
            case "ORDER":
                return "订单消息";
            case "PROMOTION":
                return "优惠活动";
            case "SYSTEM":
                return "系统通知";
            default:
                return "通知";
        }
    }
    
    /**
     * 获取类型颜色
     */
    public int getTypeColor() {
        switch (type) {
            case "ORDER":
                return 0xFFF97316; // 橙色
            case "PROMOTION":
                return 0xFF10B981; // 绿色
            case "SYSTEM":
                return 0xFF3B82F6; // 蓝色
            default:
                return 0xFF64748B; // 灰色
        }
    }
}
