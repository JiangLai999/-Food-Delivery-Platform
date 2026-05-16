package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告实体类
 */
@Data
@TableName("system_notice")
public class SystemNotice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /**
     * 公告类型：0-系统通知，1-活动通知，2-维护通知
     */
    private Integer noticeType;

    /**
     * 目标对象：0-全部，1-用户，2-商家
     */
    private Integer targetType;

    /**
     * 状态：0-下线，1-发布
     */
    private Integer status;

    private Long publisherId;

    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
