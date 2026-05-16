package com.fooddelivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.AdminLoginDTO;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.entity.FoodCategory;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.entity.MerchantCategory;
import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.SystemNotice;
import com.fooddelivery.entity.User;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     */
    String login(AdminLoginDTO dto);

    /**
     * 获取管理员信息
     */
    Admin getAdminById(Long adminId);

    /**
     * 审核商家
     */
    void approveMerchant(Long merchantId, Integer status, String reason);

    /**
     * 获取待审核商家列表
     */
    Page<Merchant> getPendingMerchants(Integer pageNum, Integer pageSize);

    /**
     * 管理用户
     */
    void manageUser(Long userId, Integer status);

    /**
     * 获取用户列表
     */
    Page<User> getUserList(String phone, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 发布系统公告
     */
    void publishNotice(SystemNotice notice);

    /**
     * 获取系统公告列表
     */
    Page<SystemNotice> getNoticeList(Integer pageNum, Integer pageSize);

    /**
     * 数据备份
     */
    String backupData();

    /**
     * 下载备份文件
     */
    void downloadBackup(String filename, javax.servlet.http.HttpServletResponse response) throws Exception;

    /**
     * 获取商家列表
     */
    Page<Merchant> getMerchantList(String keyword, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据ID获取商家
     */
    Merchant getMerchantById(Long id);

    /**
     * 更新商家状态
     */
    void updateMerchantStatus(Long id, Integer status);

    /**
     * 获取商家分类列表
     */
    List<MerchantCategory> getMerchantCategoryList();

    /**
     * 添加商家分类
     */
    void addMerchantCategory(MerchantCategory category);

    /**
     * 更新商家分类
     */
    void updateMerchantCategory(MerchantCategory category);

    /**
     * 删除商家分类
     */
    void deleteMerchantCategory(Long id);

    /**
     * 获取餐品分类列表（保留原有方法）
     */
    List<FoodCategory> getCategoryList();

    /**
     * 添加餐品分类
     */
    void addCategory(FoodCategory category);

    /**
     * 更新餐品分类
     */
    void updateCategory(FoodCategory category);

    /**
     * 删除餐品分类
     */
    void deleteCategory(Long id);

    /**
     * 更新公告
     */
    void updateNotice(SystemNotice notice);

    /**
     * 删除公告
     */
    void deleteNotice(Long id);

    /**
     * 修改管理员密码
     */
    boolean changePassword(Long adminId, String oldPassword, String newPassword);

    /**
     * 发送验证码
     */
    void sendVerifyCode(String username);

    /**
     * 获取验证码（用于找回密码）
     */
    String getVerifyCode(String username);

    /**
     * 重置密码
     */
    void resetPassword(String username, String code, String newPassword);

    /**
     * 获取待审核评价列表
     */
    Page<Review> getPendingReviews(Integer pageNum, Integer pageSize);

    /**
     * 获取所有评价列表
     */
    Page<Review> getAllReviews(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 审核评价
     */
    void auditReview(Long reviewId, Integer status);

    /**
     * 删除评价
     */
    void deleteReview(Long reviewId);

    /**
     * 获取平台统计数据
     */
    java.util.Map<String, Object> getPlatformStatistics();
}
