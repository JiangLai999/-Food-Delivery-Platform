package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
    @TableName("orders")
public class Order implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long merchantId;

    private Long riderId;

    private BigDecimal totalAmount;

    private BigDecimal deliveryFee;

    private BigDecimal packFee;

    private BigDecimal couponDiscount;

    private BigDecimal finalAmount;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private BigDecimal receiverLongitude;

    private BigDecimal receiverLatitude;

    private String remark;

    /**
     * 订单状态：0-待支付，1-待接单，2-已接单，3-配送中，4-已完成，5-已取消
     */
    private Integer status;

    /**
     * 支付状态：0-未支付，1-已支付
     */
    private Integer payStatus;

    private LocalDateTime payTime;

    private LocalDateTime acceptTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime completeTime;

    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
