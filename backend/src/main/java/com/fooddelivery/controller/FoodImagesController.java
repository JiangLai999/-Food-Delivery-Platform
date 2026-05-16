package com.fooddelivery.controller;

import com.fooddelivery.service.FoodItemService;
import com.fooddelivery.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/food/images")
public class FoodImagesController {

    @Autowired
    private FoodItemService foodItemService;

    @GetMapping("/{foodId}")
    public Result<List<String>> getFoodImages(@PathVariable Long foodId) {
        // Attempt to fetch FoodItem and split image field if CSV
        try {
            var food = foodItemService.getFoodById(foodId);
            if (food == null) return Result.error("餐品不存在");
            String img = food.getImage();
            if (img == null || img.isEmpty()) return Result.success(Arrays.asList());
            List<String> list = Arrays.asList(img.split(","));
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取图片失败: " + e.getMessage());
        }
    }
}
