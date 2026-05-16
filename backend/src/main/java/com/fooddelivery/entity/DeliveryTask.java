package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送任务实体类
 */
@Data
@TableName("delivery_task")
public class DeliveryTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long merchantId;

    private Long riderId;

    @JsonProperty("pickupLongitude")
    private BigDecimal pickupLongitude;

    @JsonProperty("pickupLatitude")
    private BigDecimal pickupLatitude;

    @JsonProperty("deliveryLongitude")
    private BigDecimal deliveryLongitude;

    @JsonProperty("deliveryLatitude")
    private BigDecimal deliveryLatitude;

    /**
     * 路线数据（JSON格式存储路径点）
     */
    private String routeData;

    /**
     * 当前路线点位置索引
     */
    private Integer currentPosition;

    /**
     * 预计送达时间（分钟）
     */
    private Integer estimatedTime;

    /**
     * 总距离（米）
     */
    private Integer totalDistance;

    /**
     * 状态：0-待取餐，1-配送中，2-已送达
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
