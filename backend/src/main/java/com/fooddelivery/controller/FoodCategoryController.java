package com.fooddelivery.controller;

import com.fooddelivery.entity.FoodCategory;
import com.fooddelivery.service.FoodCategoryService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 餐品分类Controller
 */
@Slf4j
@Tag(name = "餐品分类接口", description = "餐品分类管理接口")
@RestController
@RequestMapping("/food-category")
public class FoodCategoryController {

    @Autowired
    private FoodCategoryService foodCategoryService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "创建分类")
    @PostMapping("/create")
    public Result<String> createCategory(@RequestHeader("Authorization") String token,
                                         @RequestBody FoodCategory category) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        category.setMerchantId(merchantId);
        foodCategoryService.createCategory(category);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新分类")
    @PutMapping("/update")
    public Result<String> updateCategory(@RequestHeader("Authorization") String token,
                                         @RequestBody FoodCategory category) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        category.setMerchantId(merchantId);
        foodCategoryService.updateCategory(category);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{categoryId}")
    public Result<String> deleteCategory(@RequestHeader("Authorization") String token,
                                         @Parameter(description = "分类ID") @PathVariable Long categoryId) {
        Long merchantId = jwtUtil.getUserIdFromToken(token);
        foodCategoryService.deleteCategory(categoryId, merchantId);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取商家所有分类")
    @GetMapping("/merchant/{merchantId}")
    public Result<List<FoodCategory>> getMerchantCategories(
            @Parameter(description = "商家ID") @PathVariable Long merchantId) {
        List<FoodCategory> categories = foodCategoryService.getMerchantCategories(merchantId);
        return Result.success(categories);
    }

    @Operation(summary = "获取所有分类（公共接口）")
    @GetMapping("/list")
    public Result<List<FoodCategory>> getAllCategories() {
        List<FoodCategory> categories = foodCategoryService.getAllCategories();
        return Result.success(categories);
    }
}