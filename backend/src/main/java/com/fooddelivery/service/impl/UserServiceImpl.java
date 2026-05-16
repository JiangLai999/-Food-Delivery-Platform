package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.config.GlobalExceptionHandler;
import com.fooddelivery.dto.UserLoginDTO;
import com.fooddelivery.dto.UserRegisterDTO;
import com.fooddelivery.entity.Coupon;
import com.fooddelivery.entity.User;
import com.fooddelivery.entity.UserCoupon;
import com.fooddelivery.mapper.CouponMapper;
import com.fooddelivery.mapper.UserCouponMapper;
import com.fooddelivery.mapper.UserMapper;
import com.fooddelivery.service.UserService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户Service实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // 新用户注册时赠送的优惠券ID列表
    private static final List<Long> NEW_USER_COUPON_IDS = Arrays.asList(1L, 2L, 3L);

    // 模拟验证码存储（实际项目中应使用Redis）
    private static final Map<String, String> CODE_MAP = new HashMap<>();
    // 记录验证码发送时间，辅助实现 TTL 机制
    private static final Map<String, Long> CODE_TIME = new HashMap<>();
    private static final long CODE_TTL_MS = 5 * 60 * 1000L; // 5 分钟


    @Override
    @Transactional
    public void register(UserRegisterDTO dto) {
        // 验证验证码
        if (!verifyCode(dto.getPhone(), dto.getCode())) {
            throw new GlobalExceptionHandler.BusinessException("验证码错误");
        }

        // 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new GlobalExceptionHandler.BusinessException("该手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(MD5Util.encrypt(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + dto.getPhone().substring(7));
        user.setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功，手机号：{}", dto.getPhone());

        // 为新用户赠送优惠券
        distributeCoupons(user.getId());
    }

    /**
     * 为用户发放优惠券
     */
    private void distributeCoupons(Long userId) {
        for (Long couponId : NEW_USER_COUPON_IDS) {
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon != null && coupon.getStatus() == 1) {
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setUserId(userId);
                userCoupon.setCouponId(couponId);
                userCoupon.setStatus(0); // 未使用
                userCoupon.setCreateTime(LocalDateTime.now());
                userCouponMapper.insert(userCoupon);
                log.info("为用户{}发放优惠券: {}", userId, coupon.getTitle());
            }
        }
    }

    @Override
    public Map<String, Object> login(UserLoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new GlobalExceptionHandler.BusinessException("用户不存在");
        }

        // 检查登录类型：1-验证码登录，0-密码登录（默认）
        Integer loginType = dto.getLoginType();
        if (loginType != null && loginType == 1) {
            // 验证码登录
            if (!verifyCode(dto.getPhone(), dto.getPassword())) {
                throw new GlobalExceptionHandler.BusinessException("验证码错误");
            }
        } else {
            // 密码登录
            if (!MD5Util.verify(dto.getPassword(), user.getPassword())) {
                throw new GlobalExceptionHandler.BusinessException("密码错误");
            }
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new GlobalExceptionHandler.BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), "user");
        log.info("用户登录成功，用户ID：{}", user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        return result;
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalExceptionHandler.BusinessException("用户不存在");
        }
        // 清空密码字段
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new GlobalExceptionHandler.BusinessException("用户ID不能为空");
        }

        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new GlobalExceptionHandler.BusinessException("用户不存在");
        }

        // 不允许修改密码（密码修改应该单独接口）
        user.setPassword(null);
        user.setPhone(null);

        userMapper.updateById(user);
        log.info("用户信息更新成功，用户ID：{}", user.getId());
    }

    @Override
    public void sendVerifyCode(String phone) {
        // 生成6位验证码（实际项目中应该调用短信服务）
        // 这里使用固定验证码123456用于演示
        String normalized = normalizePhone(phone);
        String code = "123456";
        CODE_MAP.put(normalized, code);
        CODE_TIME.put(normalized, System.currentTimeMillis());
        log.info("发送验证码成功，手机号：{}，验证码：{}", normalized, code);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String normalized = normalizePhone(phone);
        String savedCode = CODE_MAP.get(normalized);
        Long sentTime = CODE_TIME.get(normalized);
        long ttlLeft = CODE_TTL_MS - (sentTime == null ? 0 : (System.currentTimeMillis() - sentTime));
        log.info("verifyCode called. phone={} normalized={}, codeInput={}, savedCode={}, ttlLeftMs={}", phone, normalized, code, savedCode, ttlLeft);
        if (savedCode == null || sentTime == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - sentTime > CODE_TTL_MS) {
            CODE_MAP.remove(normalized);
            CODE_TIME.remove(normalized);
            return false;
        }
        return savedCode.equals(code);
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.trim();
        if (p.startsWith("+86")) {
            p = p.substring(3);
        }
        p = p.replaceAll("\\s+", "");
        return p;
    }

    @Override
    public void resetPassword(String phone, String code, String newPassword) {
        log.info("开始重置密码，手机号：{}, 输入验证码：{}", phone, code);
        
        // 验证手机号格式
        if (phone == null || phone.length() != 11) {
            log.warn("手机号格式错误：{}", phone);
            throw new GlobalExceptionHandler.BusinessException("手机号格式不正确");
        }
        
        // 验证新密码格式
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            log.warn("密码长度不符合要求：{}", newPassword.length());
            throw new GlobalExceptionHandler.BusinessException("密码长度必须在6-20位之间");
        }
        
        log.info("参数验证通过，开始验证码验证...");

        // 验证验证码
        if (!verifyCode(phone, code)) {
            log.warn("验证码验证失败，手机号：{}, 输入验证码：{}", phone, code);
            throw new GlobalExceptionHandler.BusinessException("验证码错误或已过期\n测试验证码：123456");
        }

        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            log.warn("用户不存在，手机号：{}", phone);
            throw new GlobalExceptionHandler.BusinessException("用户不存在，请先注册");
        }
        
        log.info("找到用户：{}，ID：{}", user.getNickname(), user.getId());

        // 更新密码（MD5加密）
        String encryptedPassword = MD5Util.encrypt(newPassword);
        user.setPassword(encryptedPassword);
        userMapper.updateById(user);
        
        log.info("用户密码重置成功，手机号：{}，用户ID：{}", phone, user.getId());
    }

    @Override
    public void banUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalExceptionHandler.BusinessException("用户不存在");
        }
        user.setStatus(0); // 停用
        userMapper.updateById(user);
        log.info("用户已封禁：{}", userId);
    }

    @Override
    public void unbanUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalExceptionHandler.BusinessException("用户不存在");
        }
        user.setStatus(1); // 启用
        userMapper.updateById(user);
        log.info("用户已解封：{}", userId);
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.fooddelivery.entity.User> getUsersPage(int pageNum, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.fooddelivery.entity.User> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fooddelivery.entity.User> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void distributeCouponsToAllUsers() {
        // 获取所有用户
        List<User> allUsers = userMapper.selectList(null);
        log.info("开始为所有用户发放优惠券，用户总数：{}", allUsers.size());

        int distributedCount = 0;
        for (User user : allUsers) {
            // 检查用户是否已有优惠券
            LambdaQueryWrapper<UserCoupon> ucWrapper = new LambdaQueryWrapper<>();
            ucWrapper.eq(UserCoupon::getUserId, user.getId());
            List<UserCoupon> existingCoupons = userCouponMapper.selectList(ucWrapper);

            if (existingCoupons.isEmpty()) {
                // 为没有优惠券的用户发放
                distributeCoupons(user.getId());
                distributedCount++;
            }
        }
        log.info("优惠券发放完成，共为{}个用户发放了优惠券", distributedCount);
    }
}
