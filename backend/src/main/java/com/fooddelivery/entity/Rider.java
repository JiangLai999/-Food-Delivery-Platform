package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 骑手实体类
 */
@Data
@TableName("rider")
public class Rider implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String riderName;

    private String phone;

    /**
     * 状态：0-休息，1-待接单，2-配送中
     */
    private Integer status;

    private BigDecimal currentLongitude;

    private BigDecimal currentLatitude;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
