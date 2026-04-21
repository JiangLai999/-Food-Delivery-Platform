package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.dto.AdminLoginDTO;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.entity.FoodCategory;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.entity.MerchantCategory;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.SystemNotice;
import com.fooddelivery.entity.User;
import com.fooddelivery.mapper.AdminMapper;
import com.fooddelivery.mapper.FoodCategoryMapper;
import com.fooddelivery.mapper.MerchantCategoryMapper;
import com.fooddelivery.mapper.MerchantMapper;
import com.fooddelivery.mapper.OrderMapper;
import com.fooddelivery.mapper.ReviewMapper;
import com.fooddelivery.mapper.SystemNoticeMapper;
import com.fooddelivery.mapper.UserMapper;
import com.fooddelivery.service.AdminService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fooddelivery.websocket.NativeWebSocketServer;

/**
 * 管理员服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {

    // Manual logger since Lombok might not work
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Autowired
    private FoodCategoryMapper foodCategoryMapper;

    @Autowired
    private MerchantCategoryMapper merchantCategoryMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public String login(AdminLoginDTO dto) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, dto.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);

        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }

        if (!MD5Util.encrypt(dto.getPassword()).equals(admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (admin.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(admin.getId(), "admin");
        log.info("管理员登录成功: {}", admin.getUsername());

        return token;
    }

    @Override
    public Admin getAdminById(Long adminId) {
        return adminMapper.selectById(adminId);
    }

    @Override
    @Transactional
    public void approveMerchant(Long merchantId, Integer status, String reason) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }

        merchant.setStatus(status);
        if (status == 0 && reason != null) {
            merchant.setRejectReason(reason);
        }
        merchantMapper.updateById(merchant);

        log.info("审核商家 {}, 状态: {}, 原因: {}", merchantId, status, reason);
    }

    @Override
    public Page<Merchant> getPendingMerchants(Integer pageNum, Integer pageSize) {
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Merchant::getStatus, 0, 4) // 待审核(0) 和已拒绝(4)
                .orderByAsc(Merchant::getCreateTime);
        return merchantMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void manageUser(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        log.info("管理用户 {}, 状态: {}", userId, status);
    }

    @Override
    public Page<User> getUserList(String phone, Integer status, Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(phone)) {
            wrapper.like(User::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public void publishNotice(SystemNotice notice) {
        notice.setStatus(1); // 发布状态
        notice.setPublishTime(LocalDateTime.now());
        systemNoticeMapper.insert(notice);

        log.info("发布系统公告: {}", notice.getTitle());
    }

    @Override
    public Page<SystemNotice> getNoticeList(Integer pageNum, Integer pageSize) {
        Page<SystemNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemNotice::getPublishTime);
        return systemNoticeMapper.selectPage(page, wrapper);
    }

    @Override
    public String backupData() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "food_delivery_backup_" + timestamp + ".sql";

        // 从JDBC URL中提取数据库名
        // String dbName = extractDatabaseName(dbUrl);
        String dbName = "food_delivery"; // 硬编码数据库名
        log.info("数据库名: {}, JDBC URL: {}", dbName, dbUrl);

        // 创建备份目录
        String backupDir = System.getProperty("user.dir") + File.separator + "backups";
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String backupFilePath = backupDir + File.separator + backupFileName;

        try {
            // 构建mysqldump命令，使用完整路径
            String mysqldumpPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump";
            String command;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows系统 - 使用带引号的完整路径
                command = String.format(
                    "\"%s\" -u%s -p%s --databases %s --result-file=\"%s\"",
                    mysqldumpPath, dbUsername, dbPassword, dbName, backupFilePath
                );
            } else {
                // Linux/Mac系统
                command = String.format(
                    "mysqldump -u%s -p%s --databases %s --result-file='%s'",
                    dbUsername, dbPassword, dbName, backupFilePath
                );
            }

            log.info("执行备份命令: {}", command);

            // 执行备份命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder errorOutput = new StringBuilder();
            StringBuilder output = new StringBuilder();
            
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("数据备份成功: {}", backupFilePath);
                return backupFilePath;
            } else {
                log.error("数据备份失败，退出码: {}, 错误信息: {}", exitCode, errorOutput.toString());
                throw new RuntimeException("数据备份失败: " + errorOutput.toString());
            }

        } catch (IOException | InterruptedException e) {
            log.error("执行数据备份时发生异常", e);
            throw new RuntimeException("数据备份失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadBackup(String filename, javax.servlet.http.HttpServletResponse response) throws Exception {
        String backupDir = System.getProperty("user.dir") + File.separator + "backups";
        File backupFile = new File(backupDir + File.separator + filename);
        
        if (!backupFile.exists()) {
            throw new RuntimeException("备份文件不存在");
        }
        
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentLengthLong(backupFile.length());
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(backupFile);
             java.io.OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    /**
     * 从JDBC URL中提取数据库名
     */
    private String extractDatabaseName(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/food_delivery?...
        try {
            String[] parts = jdbcUrl.split("/");
            String dbPart = parts[parts.length - 1];
            if (dbPart.contains("?")) {
                return dbPart.substring(0, dbPart.indexOf("?"));
            }
            return dbPart;
        } catch (Exception e) {
            log.error("解析数据库名失败", e);
            return "food_delivery";
        }
    }

    @Override
    public Page<Merchant> getMerchantList(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Merchant::getMerchantName, keyword);
        }
        if (status != null) {
            wrapper.eq(Merchant::getStatus, status);
        }

        wrapper.orderByDesc(Merchant::getCreateTime);
        return merchantMapper.selectPage(page, wrapper);
    }

    @Override
    public Merchant getMerchantById(Long id) {
        return merchantMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateMerchantStatus(Long id, Integer status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }
        merchant.setStatus(status);
        merchantMapper.updateById(merchant);
        log.info("更新商家状态: {}, 状态: {}", id, status);
    }

    @Override
    public List<MerchantCategory> getMerchantCategoryList() {
        LambdaQueryWrapper<MerchantCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MerchantCategory::getSortOrder);
        return merchantCategoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void addMerchantCategory(MerchantCategory category) {
        category.setCreateTime(LocalDateTime.now());
        merchantCategoryMapper.insert(category);
        log.info("添加商家分类: {}", category.getCategoryName());
    }

    @Override
    @Transactional
    public void updateMerchantCategory(MerchantCategory category) {
        merchantCategoryMapper.updateById(category);
        log.info("更新商家分类: {}", category.getId());
    }

    @Override
    @Transactional
    public void deleteMerchantCategory(Long id) {
        merchantCategoryMapper.deleteById(id);
        log.info("删除商家分类: {}", id);
    }

    @Override
    public List<FoodCategory> getCategoryList() {
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FoodCategory::getSortOrder);
        return foodCategoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void addCategory(FoodCategory category) {
        category.setCreateTime(LocalDateTime.now());
        foodCategoryMapper.insert(category);
        log.info("添加分类: {}", category.getCategoryName());
    }

    @Override
    @Transactional
    public void updateCategory(FoodCategory category) {
        foodCategoryMapper.updateById(category);
        log.info("更新分类: {}", category.getId());
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        foodCategoryMapper.deleteById(id);
        log.info("删除分类: {}", id);
    }

    @Override
    @Transactional
    public void updateNotice(SystemNotice notice) {
        systemNoticeMapper.updateById(notice);
        log.info("更新公告: {}", notice.getId());
    }

    @Override
    @Transactional
    public void deleteNotice(Long id) {
        systemNoticeMapper.deleteById(id);
        log.info("删除公告: {}", id);
    }

    @Override
    @Transactional
    public boolean changePassword(Long adminId, String oldPassword, String newPassword) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 验证旧密码
        String oldPasswordHash = MD5Util.encrypt(oldPassword);
        if (!admin.getPassword().equals(oldPasswordHash)) {
            return false;
        }
        
        // 更新密码
        String newPasswordHash = MD5Util.encrypt(newPassword);
        admin.setPassword(newPasswordHash);
        adminMapper.updateById(admin);
        log.info("管理员修改密码成功: {}", adminId);
        return true;
    }

    // 验证码存储（生产环境应使用Redis）
    private static final java.util.Map<String, String> CODE_MAP = new java.util.HashMap<>();

    @Override
    public void sendVerifyCode(String username) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        Admin admin = adminMapper.selectOne(wrapper);
        
        if (admin == null) {
            throw new RuntimeException("管理员账号不存在");
        }
        
        String code = String.format("%06d", (int) ((Math.random() * 9 + 1) * 100000));
        CODE_MAP.put(username, code);
        log.info("管理员找回密码验证码: {}, {}", username, code);
    }

    // 获取验证码（用于前端展示）
    public String getVerifyCode(String username) {
        return CODE_MAP.get(username);
    }

    @Override
    public void resetPassword(String username, String code, String newPassword) {
        log.info("重置密码: username={}, inputCode={}", username, code);
        String savedCode = CODE_MAP.get(username);
        log.info("savedCode={}", savedCode);
        if (savedCode == null || !savedCode.equals(code)) {
            log.error("验证码错误: expected={}, actual={}", savedCode, code);
            throw new RuntimeException("验证码错误");
        }
        
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        Admin admin = adminMapper.selectOne(wrapper);
        
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        admin.setPassword(MD5Util.encrypt(newPassword));
        adminMapper.updateById(admin);
        CODE_MAP.remove(username);
        log.info("管理员密码重置成功: {}", username);
    }

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Review> getPendingReviews(Integer pageNum, Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Review> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getStatus, 0); // 只返回待审核
        wrapper.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Review> getAllReviews(Integer pageNum, Integer pageSize, Integer status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Review> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void auditReview(Long reviewId, Integer status) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        review.setStatus(status);
        reviewMapper.updateById(review);
        log.info("审核评价: {}, 状态: {}", reviewId, status);
        
        if (status == 1) {
            if (review.getMerchantId() != null) {
                try {
                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("type", "REVIEW_APPROVED");
                    notificationData.put("reviewId", reviewId);
                    notificationData.put("orderId", review.getOrderId());
                    notificationData.put("content", review.getContent());
                    notificationData.put("rating", review.getRating());
                    notificationData.put("timestamp", review.getCreateTime());
                    NativeWebSocketServer.sendChatMessageToMerchant(review.getMerchantId(), notificationData);
                    log.info("已通知商家 {} 评价 {} 已通过审核", review.getMerchantId(), reviewId);
                } catch (Exception e) {
                    log.error("通知商家失败", e);
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewMapper.deleteById(reviewId);
        log.info("删除评价: {}", reviewId);
    }

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public java.util.Map<String, Object> getPlatformStatistics() {
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        
        // 今日订单数
        LocalDateTime startOfToday = java.time.LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreateTime, startOfToday);
        statistics.put("todayOrderCount", orderMapper.selectCount(todayWrapper));
        
        // 今日销售额
        java.util.List<Order> todayOrders = orderMapper.selectList(todayWrapper);
        double todaySales = todayOrders.stream()
            .mapToDouble(o -> o.getFinalAmount().doubleValue())
            .sum();
        statistics.put("todaySales", todaySales);
        
        // 用户总数
        statistics.put("totalUserCount", userMapper.selectCount(null));
        
        // 活跃商家数
        LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.eq(Merchant::getStatus, 1);
        statistics.put("activeMerchantCount", merchantMapper.selectCount(merchantWrapper));
        
        return statistics;
    }
}
