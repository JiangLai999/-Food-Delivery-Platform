package com.fooddelivery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AdminNoticeDTO {
    @NotBlank
    public String title;
    @NotBlank
    public String content;
    public String status;
}
