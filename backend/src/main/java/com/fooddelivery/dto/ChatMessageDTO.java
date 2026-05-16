package com.fooddelivery.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 聊天消息 DTO（用于在线咨询等场景）
 */
@Data
public class ChatMessageDTO {

    @NotNull(message = "接收方ID不能为空")
    private Long toUserId;

    @NotNull(message = "接收方类型不能为空")
    private Integer toUserType; // 1-用户，2-商家，3-客服

    @NotNull(message = "消息内容不能为空")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private Integer contentType; // 0-文本，1-图片
}
