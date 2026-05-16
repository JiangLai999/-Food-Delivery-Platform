package com.fooddelivery.dto;

import lombok.Data;

/**
 * 创建配送任务 DTO
 */
@Data
public class DeliveryCreateDTO {
    private Long orderId;
    private Long riderId;
}
