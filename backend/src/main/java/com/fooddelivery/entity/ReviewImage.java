package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价图片实体类
 */
@Data
@TableName("review_image")
public class ReviewImage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reviewId;

    private String imageUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
