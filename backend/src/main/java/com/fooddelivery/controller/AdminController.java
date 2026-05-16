package com.fooddelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.AdminLoginDTO;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.SystemNotice;
import com.fooddelivery.entity.User;
import com.fooddelivery.mapper.FoodItemMapper;
import com.fooddelivery.mapper.OrderMapper;
import com.fooddelivery.service.AdminService;
import com.fooddelivery.service.MerchantService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员Controller
 */
@Slf4j
@Tag(name = "管理员接口", description = "管理员相关操作接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DataSource dataSource;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid AdminLoginDTO dto) {
        String token = adminService.login(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", dto.getUsername());
        data.put("userId", 1L);
        data.put("userType", 3);
        return Result.success("登录成功", data);
    }

    @Operation(summary = "获取商家列表")
    @GetMapping("/merchants")
    public Result<Page<Merchant>> getMerchants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Merchant> page = adminService.getMerchantList(keyword, status, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "审核商家")
    @PostMapping("/approve-merchant/{merchantId}")
    public Result<String> approveMerchant(
            @Parameter(description = "商家ID") @PathVariable Long merchantId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        adminService.approveMerchant(merchantId, status, reason);
        return Result.success("审核成功");
    }

    @Operation(summary = "更新商家状态")
    @PutMapping("/merchants/{id}/status")
    public Result<String> updateMerchantStatus(
            @Parameter(description = "商家ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        adminService.updateMerchantStatus(id, status);
        return Result.success("操作成功");
    }

    @Operation(summary = "获取待审核商家列表")
    @GetMapping("/pending-merchants")
    public Result<Page<Merchant>> getPendingMerchants(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Merchant> page = adminService.getPendingMerchants(pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<Page<User>> getUsers(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = adminService.getUserList(phone, status, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/users/{id}/status")
    public Result<String> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        adminService.manageUser(id, status);
        return Result.success("操作成功");
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/users/{id}")
    public Result<User> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        User user = adminService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @Operation(summary = "发布公告")
    @PostMapping("/publish-notice")
    public Result<String> publishNotice(@RequestBody SystemNotice notice) {
        adminService.publishNotice(notice);
        return Result.success("发布成功");
    }

    @Operation(summary = "获取公告列表")
    @GetMapping("/notices")
    public Result<Page<SystemNotice>> getNotices(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SystemNotice> notices = adminService.getNoticeList(pageNum, pageSize);
        return Result.success(notices);
    }

    @Operation(summary = "更新公告")
    @PutMapping("/notices/{id}")
    public Result<String> updateNotice(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @RequestBody SystemNotice notice) {
        notice.setId(id);
        adminService.updateNotice(notice);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/notices/{id}")
    public Result<String> deleteNotice(@Parameter(description = "公告ID") @PathVariable Long id) {
        adminService.deleteNotice(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "修改管理员密码")
    @PutMapping("/password")
    public Result<String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long adminId = jwtUtil.getUserIdFromToken(token);
        boolean success = adminService.changePassword(adminId, oldPassword, newPassword);
        if (success) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("原密码错误");
        }
    }

    @Operation(summary = "发送验证码（找回密码）")
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestParam String username) {
        adminService.sendVerifyCode(username);
        // 返回验证码供前端展示
        String verifyCode = adminService.getVerifyCode(username);
        return Result.success("验证码已发送", verifyCode);
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(
            @RequestParam String username,
            @RequestParam String code,
            @RequestParam String newPassword) {
        adminService.resetPassword(username, code, newPassword);
        return Result.success("密码重置成功");
    }

    @Operation(summary = "获取评价列表")
    @GetMapping("/reviews")
    public Result<Page<Map<String, Object>>> getReviews(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "审核状态: 0-待审核, 1-已通过, 2-已拒绝") @RequestParam(required = false) Integer status) {
        Page<com.fooddelivery.entity.Review> reviewPage = adminService.getAllReviews(pageNum, pageSize, status);
        
        // 转换为包含订单详情的Map
        Page<Map<String, Object>> result = new Page<>(reviewPage.getCurrent(), reviewPage.getSize());
        result.setTotal(reviewPage.getTotal());
        
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (Review review : reviewPage.getRecords()) {
            Map<String, Object> reviewMap = new java.util.HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("orderId", review.getOrderId());
            reviewMap.put("userId", review.getUserId());
            reviewMap.put("merchantId", review.getMerchantId());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("tasteRating", review.getTasteRating());
            reviewMap.put("portionRating", review.getPortionRating());
            reviewMap.put("content", review.getContent());
            reviewMap.put("isAnonymous", review.getIsAnonymous());
            reviewMap.put("status", review.getStatus());
            reviewMap.put("replyContent", review.getReplyContent());
            reviewMap.put("replyTime", review.getReplyTime());
            reviewMap.put("createTime", review.getCreateTime());
            
            // 获取订单号
            if (review.getOrderId() != null) {
                Order order = orderMapper.selectById(review.getOrderId());
                if (order != null) {
                    reviewMap.put("orderNo", order.getOrderNo());
                }
            }
            
            records.add(reviewMap);
        }
        result.setRecords(records);
        
        return Result.success(result);
    }

    @Operation(summary = "获取待审核评价列表")
    @GetMapping("/pending-reviews")
    public Result<Page<com.fooddelivery.entity.Review>> getPendingReviews(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<com.fooddelivery.entity.Review> page = adminService.getPendingReviews(pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "审核评价")
    @PostMapping("/review/{id}/audit")
    public Result<String> auditReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Parameter(description = "审核状态：1-通过，2-拒绝") @RequestParam Integer status) {
        adminService.auditReview(id, status);
        return Result.success(status == 1 ? "审核通过" : "审核拒绝");
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/reviews/{id}")
    public Result<String> deleteReview(@Parameter(description = "评价ID") @PathVariable Long id) {
        adminService.deleteReview(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "更新餐品信息")
    @PutMapping("/foods/{id}")
    public Result<String> updateFood(
            @Parameter(description = "餐品ID") @PathVariable Long id,
            @RequestBody FoodItem foodItem) {
        foodItem.setId(id);
        foodItemMapper.updateById(foodItem);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取平台统计数据")
    @GetMapping("/platform-statistics")
    public Result<Map<String, Object>> getPlatformStatistics() {
        Map<String, Object> statistics = adminService.getPlatformStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取商家分类列表")
    @GetMapping("/categories")
    public Result<List<com.fooddelivery.entity.MerchantCategory>> getCategories() {
        return Result.success(adminService.getMerchantCategoryList());
    }

    @Operation(summary = "获取所有商家分类列表（前端使用）")
    @GetMapping("/categories/ops/all")
    public Result<List<com.fooddelivery.entity.MerchantCategory>> getAllCategories() {
        return Result.success(adminService.getMerchantCategoryList());
    }

    @Operation(summary = "添加商家分类")
    @PostMapping("/categories/ops/add")
    public Result<String> addCategory(@RequestBody com.fooddelivery.entity.MerchantCategory category) {
        adminService.addMerchantCategory(category);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新商家分类")
    @PutMapping("/categories/ops/update/{id}")
    public Result<String> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @RequestBody com.fooddelivery.entity.MerchantCategory category) {
        category.setId(id);
        adminService.updateMerchantCategory(category);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除商家分类")
    @DeleteMapping("/categories/ops/delete/{id}")
    public Result<String> deleteCategory(@Parameter(description = "分类ID") @PathVariable Long id) {
        adminService.deleteMerchantCategory(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "修复数据库-添加评价匿名字段")
    @PostMapping("/fix-db")
    public Result<String> fixDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 检查字段是否存在
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='food_delivery' AND TABLE_NAME='review' AND COLUMN_NAME='is_anonymous'");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("ALTER TABLE food_delivery.review ADD COLUMN is_anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名评价' AFTER content");
                return Result.success("字段添加成功");
            } else {
                return Result.success("字段已存在，无需添加");
            }
        } catch (Exception e) {
            log.error("修复数据库失败", e);
            return Result.error("修复失败: " + e.getMessage());
        }
    }

    @Operation(summary = "清理无效聊天记录")
    @PostMapping("/cleanup-chat")
    public Result<String> cleanupChat() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 清空所有聊天记录
            stmt.executeUpdate("DELETE FROM chat_message");
            
            return Result.success("聊天记录已清空");
        } catch (Exception e) {
            log.error("清理失败", e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }

    @Operation(summary = "数据备份")
    @PostMapping("/backup")
    public Result<String> backupData() {
        String backupFileName = adminService.backupData();
        return Result.success("备份成功", backupFileName);
    }

    @Operation(summary = "下载备份文件")
    @GetMapping("/backup/download")
    public void downloadBackup(@RequestParam String filename, HttpServletResponse response) {
        try {
            adminService.downloadBackup(filename, response);
        } catch (Exception e) {
            log.error("下载备份失败", e);
        }
    }
}
