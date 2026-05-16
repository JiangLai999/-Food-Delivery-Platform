package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@TableName("review")
public class Review implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long userId;

    private Long merchantId;

    /**
     * 评分：1-5星
     */
    private Integer rating;

    /**
     * 口味评分：1-5星
     */
    private Integer tasteRating;

    /**
     * 分量评分：1-5星
     */
    private Integer portionRating;

    private String content;

    private Boolean isAnonymous = false;

    /**
     * 审核状态: 0-待审核, 1-已通过, 2-已拒绝
     */
    private Integer status = 0;

    private String replyContent;

    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
