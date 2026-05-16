package com.fooddelivery.user.network;

import com.google.gson.annotations.SerializedName;

/**
 * 聊天消息请求
 */
public class ChatMessageRequest {
    
    @SerializedName("toUserId")
    private Long toUserId;
    
    @SerializedName("toUserType")
    private Integer toUserType;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("contentType")
    private Integer contentType;

    public ChatMessageRequest() {
    }

    public ChatMessageRequest(Long toUserId, Integer toUserType, String content) {
        this.toUserId = toUserId;
        this.toUserType = toUserType;
        this.content = content;
        this.contentType = 0;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
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
}
