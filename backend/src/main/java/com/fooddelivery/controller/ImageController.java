package com.fooddelivery.controller;

import com.fooddelivery.service.ImageService;
import com.fooddelivery.service.FoodItemService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fooddelivery.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private ImageService imageService;
    @Autowired
    private FoodItemService foodItemService;

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("type") String type,
                                      @RequestParam(value = "foodId", required = false) Long foodId,
                                      @RequestParam("file") MultipartFile file) {
        String subDir = "misc";
        if ("food".equalsIgnoreCase(type)) {
            subDir = (foodId != null ? ("food/" + foodId) : "food/0");
        } else if ("user-avatar".equalsIgnoreCase(type)) {
            subDir = "avatar/user";
        } else if ("merchant-avatar".equalsIgnoreCase(type)) {
            subDir = "avatar/merchant";
        } else if ("merchant-license".equalsIgnoreCase(type)) {
            subDir = "license/merchant";
        }
        try {
            String path = imageService.saveImage(file, subDir);
            // 如果是食品图片，尝试把图片追加到食品的 image 字段（CSV）以实现多图效果演示
            if (foodId != null) {
                com.fooddelivery.entity.FoodItem item = null;
                if (foodItemService != null) {
                    item = foodItemService.getFoodById(foodId);
                }
                if (item != null) {
                    List<String> images = new ArrayList<>();
                    String current = item.getImage();
                    if (current != null && !current.isEmpty()) {
                        images.addAll(Arrays.asList(current.split(",")));
                    }
                    images.add(path);
                    if (foodItemService != null) foodItemService.updateFoodImages(foodId, images);
                }
            }
            return Result.success(path);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}
