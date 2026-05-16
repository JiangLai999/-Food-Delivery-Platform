package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 骑手位置记录实体类
 */
@Data
@TableName("rider_location")
public class RiderLocation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long riderId;

    private Long orderId;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
