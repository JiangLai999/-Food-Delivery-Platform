package com.fooddelivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.MerchantLoginDTO;
import com.fooddelivery.dto.MerchantRegisterDTO;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Merchant;

import javax.validation.Valid;
import java.util.List;

/**
 * 商家Service接口
 */
public interface MerchantService {

    /**
     * 商家登录
     */
    String login(MerchantLoginDTO dto);

    /**
     * 获取商家信息
     */
    Merchant getMerchantById(Long merchantId);

    /**
     * 更新商家信息
     */
    void updateMerchant(Merchant merchant);

    /**
     * 分页查询商家列表
     */
    Page<Merchant> getMerchantPage(Integer pageNum, Integer pageSize, Long categoryId);

    /**
     * 添加餐品
     */
    void addFoodItem(FoodItem foodItem);

    /**
     * 更新餐品
     */
    void updateFoodItem(FoodItem foodItem);

    /**
     * 删除餐品
     */
    void deleteFoodItem(Long foodId);

    /**
     * 发送验证码（用于密码重置等场景）
     */
    void sendVerifyCode(String phone);

    /**
     * 重置密码（通过手机验证码）
     */
    void resetPassword(String phone, String code, String newPassword);

    /**
     * 获取商家的餐品列表
     */
    Page<FoodItem> getFoodItemPage(Long merchantId, Integer pageNum, Integer pageSize);

    Page<Merchant> searchMerchants(String keyword, Long categoryId, Integer pageNum, Integer pageSize);

    void updateBusinessStatus(Long merchantId, Integer status);

    void register(@Valid MerchantRegisterDTO dto);

    /**
     * 获取热门商家列表
     */
    List<Merchant> getHotMerchants(Integer limit);

    /**
     * 获取所有商家
     */
    List<Merchant> getAllMerchants();
}
