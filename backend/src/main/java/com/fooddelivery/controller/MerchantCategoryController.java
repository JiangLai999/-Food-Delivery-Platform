package com.fooddelivery.controller;

import com.fooddelivery.entity.FoodCategory;
import com.fooddelivery.entity.MerchantCategory;
import com.fooddelivery.service.FoodCategoryService;
import com.fooddelivery.service.MerchantCategoryService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 商家分类管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/merchant")
@Tag(name = "商家分类管理")
public class MerchantCategoryController {

    @Autowired
    private FoodCategoryService foodCategoryService;

    @Autowired
    private MerchantCategoryService merchantCategoryService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有商家分类（公开接口）
     */
    @GetMapping("/categories/all")
    @Operation(summary = "获取所有商家分类")
    public Result<List<MerchantCategory>> getAllCategories() {
        List<MerchantCategory> list = merchantCategoryService.getAllCategories();
        return Result.success(list);
    }

    /**
     * 获取商家分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取商家分类列表")
    public Result<List<FoodCategory>> getCategories(HttpServletRequest request) {
        Long merchantId = getCurrentMerchantId(request);
        if (merchantId == null) {
            return Result.error("请先登录");
        }
        
        List<FoodCategory> list = foodCategoryService.getCategoriesByMerchant(merchantId);
        return Result.success(list);
    }

    /**
     * 添加分类
     */
    @PostMapping("/category/add")
    @Operation(summary = "添加分类")
    public Result<Void> addCategory(@RequestBody FoodCategory category, HttpServletRequest request) {
        Long merchantId = getCurrentMerchantId(request);
        if (merchantId == null) {
            return Result.error("请先登录");
        }
        
        category.setMerchantId(merchantId);
        foodCategoryService.addCategory(category);
        return Result.success();
    }

    /**
     * 更新分类
     */
    @PutMapping("/category/update")
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(@RequestBody FoodCategory category, HttpServletRequest request) {
        Long merchantId = getCurrentMerchantId(request);
        if (merchantId == null) {
            return Result.error("请先登录");
        }
        
        // 验证分类是否属于当前商家
        FoodCategory existing = foodCategoryService.getById(category.getId());
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            return Result.error("分类不存在或无权限");
        }
        
        category.setMerchantId(merchantId);
        foodCategoryService.updateCategory(category);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/delete/{categoryId}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable Long categoryId, HttpServletRequest request) {
        Long merchantId = getCurrentMerchantId(request);
        if (merchantId == null) {
            return Result.error("请先登录");
        }
        
        // 验证分类是否属于当前商家
        FoodCategory existing = foodCategoryService.getById(categoryId);
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            return Result.error("分类不存在或无权限");
        }
        
        foodCategoryService.deleteCategory(categoryId);
        return Result.success();
    }

    /**
     * 更新商家营业设置
     */
    @PutMapping("/settings")
    @Operation(summary = "更新商家营业设置")
    public Result<Void> updateSettings(@RequestBody MerchantSettingsDTO dto, HttpServletRequest request) {
        Long merchantId = getCurrentMerchantId(request);
        if (merchantId == null) {
            return Result.error("请先登录");
        }
        
        foodCategoryService.updateMerchantSettings(merchantId, dto);
        return Result.success();
    }

    private Long getCurrentMerchantId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                log.error("解析token失败", e);
            }
        }
        return null;
    }
}
