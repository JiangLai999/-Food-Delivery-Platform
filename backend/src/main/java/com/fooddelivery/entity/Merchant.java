package com.fooddelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家实体类
 */
@Data
@TableName("merchant")
public class Merchant implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String password;

    private String merchantName;

    private String logo;

    private String licenseNumber;

    private String licenseImage;

    private String contactPerson;

    private String contactPhone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Long categoryId;

    private String categoryName;

    private String description;

    private String businessHours;

    private BigDecimal deliveryFee;

    private BigDecimal minOrderAmount;

    private BigDecimal avgRating;

    private Integer salesVolume;

    private Integer status;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
