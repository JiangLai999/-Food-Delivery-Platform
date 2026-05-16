package com.fooddelivery.service;

import com.fooddelivery.dto.UserLoginDTO;
import com.fooddelivery.dto.UserRegisterDTO;
import com.fooddelivery.entity.User;

import java.util.Map;

/**
 * 用户Service接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    Map<String, Object> login(UserLoginDTO dto);

    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 发送验证码
     */
    void sendVerifyCode(String phone);

    /**
     * 验证验证码
     */
    boolean verifyCode(String phone, String code);

    /**
     * 重置密码（通过手机号+验证码）
     */
    void resetPassword(String phone, String code, String newPassword);

    /**
     * 为所有现有用户发放优惠券
     */
    void distributeCouponsToAllUsers();

    // Admin helpers
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.fooddelivery.entity.User> getUsersPage(int pageNum, int pageSize);
    void banUser(Long userId);
    void unbanUser(Long userId);
}
