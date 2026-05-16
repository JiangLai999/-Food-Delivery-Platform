package com.fooddelivery.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 骑手位置信息DTO
 */
@Data
public class RiderLocationDTO implements Serializable {

    private Long riderId;

    private Long orderId;

    private String riderName;
    
    private String riderPhone;

    private BigDecimal longitude;

    private BigDecimal latitude;

    /**
     * 骑手状态：0-休息，1-待接单，2-配送中
     */
    private Integer status;

    /**
     * 距离目的地距离（米）
     */
    private Integer distance;

    /**
     * 预计送达时间（分钟）
     */
    private Integer estimatedTime;

    /**
     * 配送阶段：going_to_merchant-赶往商家, picking_up-取餐中, delivering-配送中, arrived-已送达
     */
    private String phase;

    /**
     * 距离用户距离（米）
     */
    private Integer distanceToUser;

    /**
     * 配送状态描述
     */
    private String description;
    
    /**
     * 当前配送进度位置（0-75）
     */
    private Integer currentPosition;
}
