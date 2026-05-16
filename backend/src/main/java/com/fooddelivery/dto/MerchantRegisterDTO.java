package com.fooddelivery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 商家注册DTO
 */
@Data
public class MerchantRegisterDTO implements Serializable {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "商家名称不能为空")
    private String merchantName;

    @NotBlank(message = "营业执照号不能为空")
    private String licenseNumber;

    private String licenseImage;

    private String contactPerson;

    private String contactPhone;

    private String detailAddress;
}
