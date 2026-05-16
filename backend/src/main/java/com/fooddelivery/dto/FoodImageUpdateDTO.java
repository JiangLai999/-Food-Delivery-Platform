package com.fooddelivery.dto;

import lombok.Data;
import java.util.List;

/**
 * 食品图片更新 DTO（用于批量设置图片列表）
 */
@Data
public class FoodImageUpdateDTO {
    private List<String> images;
}
