package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;

    /**
     * 发送者类型：1-用户，2-商家，3-客服
     */
    private Integer fromUserType;

    private Long toUserId;

    /**
     * 接收者类型：1-用户，2-商家，3-客服
     */
    private Integer toUserType;

    private String content;

    /**
     * 内容类型：0-文本，1-图片
     */
    private Integer contentType;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
