package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private BigDecimal amount;

    private BigDecimal minSpend;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalCount;

    private Integer remainCount;

    private Long merchantId;

    private String categoryIds;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
