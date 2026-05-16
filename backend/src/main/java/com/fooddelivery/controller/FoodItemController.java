package com.fooddelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.service.ImageService;
import com.fooddelivery.dto.FoodImageUpdateDTO;
import java.util.ArrayList;
import java.util.Arrays;
import com.fooddelivery.dto.FoodImageUpdateDTO;
import java.util.List;
import com.fooddelivery.service.FoodItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 餐品Controller
 */
@Slf4j
@Tag(name = "餐品接口", description = "餐品管理接口")
@RestController
@RequestMapping("/food")
public class FoodItemController {

    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private ImageService imageService;

    @Operation(summary = "搜索餐品（带筛选与排序）")
    @GetMapping("/search")
    public Result<Page<FoodItem>> searchFoods(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商家ID（可选）") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "分类ID（可选）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "排序（如：sales_desc、price_asc、price_desc）") @RequestParam(required = false) String sort,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FoodItem> foodPage = foodItemService.filterFoods(keyword, merchantId, categoryId, sort, page, pageSize);
        return Result.success(foodPage);
    }

    @Operation(summary = "筛选餐品（与/search功能相同）")
    @GetMapping("/filter")
    public Result<Page<FoodItem>> filterFoods(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商家ID（可选）") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "分类ID（可选）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "排序（如：sales_desc、price_asc、price_desc）") @RequestParam(required = false) String sort,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FoodItem> foodPage = foodItemService.filterFoods(keyword, merchantId, categoryId, sort, pageNum, pageSize);
        return Result.success(foodPage);
    }

    @Operation(summary = "获取热门餐品")
    @GetMapping("/hot")
    public Result<List<FoodItem>> getHotFoods(
            @Parameter(description = "返回数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        List<FoodItem> foods = foodItemService.getHotFoods(limit);
        return Result.success(foods);
    }

    @Operation(summary = "上传餐品图片")
    @PostMapping("/upload-image/{foodId}")
    public Result<String> uploadFoodImage(
            @PathVariable Long foodId,
            @RequestParam("image") MultipartFile image) {
        try {
            String path = imageService.saveImage(image, "food/" + foodId);
            FoodItem item = foodItemService.getFoodById(foodId);
            if (item != null) {
                List<String> images = new ArrayList<>();
                String current = item.getImage();
                if (current != null && !current.isEmpty()) {
                    images.addAll(Arrays.asList(current.split(",")));
                }
                images.add(path);
                foodItemService.updateFoodImages(foodId, images);
            }
            return Result.success("图片上传成功", path);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/update-image/{foodId}")
    public Result<String> updateFoodImages(@PathVariable Long foodId, @RequestBody FoodImageUpdateDTO dto) {
        List<String> images = dto != null ? dto.getImages() : null;
        foodItemService.updateFoodImages(foodId, images);
        return Result.success("图片列表更新成功");
    }

    @Operation(summary = "获取所有餐品列表（分页）")
    @GetMapping("/list")
    public Result<Page<FoodItem>> getFoodList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FoodItem> foodPage = foodItemService.getFoodList(page, pageSize);
        return Result.success(foodPage);
    }

    @Operation(summary = "根据商家ID获取餐品列表")
    @GetMapping("/merchant/{merchantId}")
    public Result<List<FoodItem>> getFoodsByMerchant(
            @Parameter(description = "商家ID") @PathVariable Long merchantId) {
        List<FoodItem> foods = foodItemService.getFoodsByMerchantId(merchantId);
        return Result.success(foods);
    }

    @Operation(summary = "根据分类获取餐品")
    @GetMapping("/category/{categoryId}")
    public Result<List<FoodItem>> getFoodsByCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId) {
        List<FoodItem> foods = foodItemService.getFoodsByCategory(categoryId);
        return Result.success(foods);
    }

    @Operation(summary = "获取餐品详情")
    @GetMapping("/{foodId}")
    public Result<FoodItem> getFoodById(
            @Parameter(description = "餐品ID") @PathVariable Long foodId) {
        FoodItem food = foodItemService.getFoodById(foodId);
        return Result.success(food);
    }
}
