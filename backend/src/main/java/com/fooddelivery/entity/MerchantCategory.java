package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家分类实体类
 */
@Data
@TableName("merchant_category")
public class MerchantCategory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String categoryName;

    private String icon;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
