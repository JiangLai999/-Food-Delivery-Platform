package com.fooddelivery.user.model;

/**
 * 系统公告模型
 */
public class SystemNotice {
    private Long id;
    private String title;
    private String content;
    private Integer type; // 0-系统通知，1-活动公告，2-维护公告
    private Integer targetType; // 0-全部，1-用户，2-商家，3-骑手
    private Integer status; // 0-未发布，1-已发布
    private Integer isRead; // 0-未读，1-已读
    private String createTime;
    private String updateTime;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    
    public Integer getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
    
    /**
     * 获取公告类型文本
     */
    public String getTypeText() {
        if (type == null) return "通知";
        switch (type) {
            case 0: return "系统通知";
            case 1: return "活动公告";
            case 2: return "维护公告";
            default: return "通知";
        }
    }
}
