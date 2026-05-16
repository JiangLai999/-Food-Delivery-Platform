package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.MerchantLoginDTO;
import com.fooddelivery.dto.MerchantRegisterDTO;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.mapper.FoodItemMapper;
import com.fooddelivery.mapper.MerchantMapper;
import java.util.HashMap;
import java.util.Map;
import com.fooddelivery.service.MerchantService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商家服务实现类
 */
@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // 简单验证码存储（演示用，生产环境应使用 Redis 等缓存系统）
    private static final Map<String, String> CODE_MAP = new HashMap<>();

    @Override
    @Transactional
    public void register(MerchantRegisterDTO dto) {
        // 检查手机号是否已注册
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getPhone, dto.getPhone());
        Merchant existingMerchant = merchantMapper.selectOne(wrapper);

        if (existingMerchant != null) {
            throw new RuntimeException("手机号已注册");
        }

        // 创建商家
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(dto, merchant);
        merchant.setPassword(MD5Util.encrypt(dto.getPassword()));
        merchant.setStatus(0); // 待审核
        merchant.setAvgRating(java.math.BigDecimal.valueOf(5.00));
        merchant.setSalesVolume(0);

        merchantMapper.insert(merchant);
        log.info("商家注册成功: {}", dto.getMerchantName());
    }

    @Override
    public String login(MerchantLoginDTO dto) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getPhone, dto.getPhone());
        Merchant merchant = merchantMapper.selectOne(wrapper);

        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }

        if (!MD5Util.encrypt(dto.getPassword()).equals(merchant.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (merchant.getStatus() == 0) {
            throw new RuntimeException("商家账号待审核");
        }

        if (merchant.getStatus() == 3) {
            throw new RuntimeException("商家账号已下架");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(merchant.getId(), "merchant");
        log.info("商家登录成功: {}", merchant.getMerchantName());

        return token;
    }

    @Override
    public void sendVerifyCode(String phone) {
        String code = "123456"; // 演示用固定验证码
        CODE_MAP.put(phone, code);
        log.info("发送验证码给商家，手机号：{}，验证码：{}", phone, code);
    }

    @Override
    public void resetPassword(String phone, String code, String newPassword) {
        String saved = CODE_MAP.get(phone);
        if (saved == null || !saved.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getPhone, phone);
        Merchant merchant = merchantMapper.selectOne(wrapper);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }
        merchant.setPassword(MD5Util.encrypt(newPassword));
        merchantMapper.updateById(merchant);
        log.info("商家密码重置成功，手机号：{}", phone);
    }

    @Override
    public Merchant getMerchantById(Long merchantId) {
        return merchantMapper.selectById(merchantId);
    }

    @Override
    @Transactional
    public void updateMerchant(Merchant merchant) {
        merchantMapper.updateById(merchant);
        log.info("商家信息更新成功: {}", merchant.getId());
    }

    @Override
    public Page<Merchant> searchMerchants(String keyword, Long categoryId, Integer pageNum, Integer pageSize) {
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Merchant::getMerchantName, keyword);
        }

        if (categoryId != null) {
            wrapper.eq(Merchant::getCategoryId, categoryId);
        }

        wrapper.eq(Merchant::getStatus, 1) // 正常营业
                .orderByDesc(Merchant::getSalesVolume);

        return merchantMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Merchant> getHotMerchants(Integer limit) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getStatus, 1)
                .orderByDesc(Merchant::getSalesVolume)
                .last("LIMIT " + limit);
        return merchantMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void updateBusinessStatus(Long merchantId, Integer status) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }

        merchant.setStatus(status);
        merchantMapper.updateById(merchant);

        log.info("商家 {} 状态更新为: {}", merchantId, status);
    }

    @Override
    public List<Merchant> getAllMerchants() {
        return merchantMapper.selectList(null);
    }

    @Override
    public Page<Merchant> getMerchantPage(Integer pageNum, Integer pageSize, Long categoryId) {
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(Merchant::getCategoryId, categoryId);
        }

        wrapper.eq(Merchant::getStatus, 1)
                .orderByDesc(Merchant::getSalesVolume);

        return merchantMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void addFoodItem(FoodItem foodItem) {
        foodItemMapper.insert(foodItem);
        log.info("添加餐品成功: {}", foodItem.getFoodName());
    }

    @Override
    @Transactional
    public void updateFoodItem(FoodItem foodItem) {
        foodItemMapper.updateById(foodItem);
        log.info("更新餐品成功: {}", foodItem.getId());
    }

    @Override
    @Transactional
    public void deleteFoodItem(Long foodId) {
        foodItemMapper.deleteById(foodId);
        log.info("删除餐品成功: {}", foodId);
    }

    @Override
    public Page<FoodItem> getFoodItemPage(Long merchantId, Integer pageNum, Integer pageSize) {
        Page<FoodItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoodItem> wrapper = new LambdaQueryWrapper<>();

        if (merchantId != null) {
            wrapper.eq(FoodItem::getMerchantId, merchantId);
        }

        wrapper.eq(FoodItem::getStatus, 1)
                .orderByDesc(FoodItem::getSalesVolume);

        return foodItemMapper.selectPage(page, wrapper);
    }
}
