-- 外卖订餐系统数据库建表脚本（无测试数据）
-- 数据库: food_delivery
-- 用于生成ER图

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `food_delivery` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `food_delivery`;

-- ========== 用户相关表 ==========

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `password` VARCHAR(64) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户收货地址表
CREATE TABLE `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

-- ========== 商家相关表 ==========

-- 商家表
CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '登录手机号',
  `password` VARCHAR(64) NOT NULL COMMENT '密码',
  `merchant_name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `logo` VARCHAR(255) DEFAULT NULL COMMENT '商家LOGO',
  `banner` VARCHAR(255) DEFAULT NULL COMMENT 'banner图',
  `license_number` VARCHAR(50) DEFAULT NULL COMMENT '营业执照号',
  `license_image` VARCHAR(255) DEFAULT NULL COMMENT '营业执照图片',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `detail_address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `category_id` BIGINT DEFAULT NULL COMMENT '商家分类ID',
  `category_name` VARCHAR(50) DEFAULT NULL COMMENT '商家分类名称',
  `description` TEXT DEFAULT NULL COMMENT '商家简介',
  `notice` TEXT DEFAULT NULL COMMENT '商家公告',
  `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间',
  `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
  `min_order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '起送价',
  `pack_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '打包费',
  `avg_rating` DECIMAL(3,2) DEFAULT 5.00 COMMENT '平均评分',
  `sales_volume` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-正常营业，2-休息中，3-已下架，4-已拒绝',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- 商家分类表
CREATE TABLE `merchant_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家分类表';

-- 餐品表
CREATE TABLE `food_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '餐品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '餐品分类ID',
  `category_name` VARCHAR(50) DEFAULT NULL COMMENT '餐品分类名称',
  `food_name` VARCHAR(100) NOT NULL COMMENT '餐品名称',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '餐品图片',
  `description` TEXT DEFAULT NULL COMMENT '餐品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `stock` INT DEFAULT 999 COMMENT '库存',
  `sales_volume` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐品表';

-- 餐品分类表
CREATE TABLE `food_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐品分类表';

-- ========== 订单相关表 ==========

-- 订单表
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `rider_id` BIGINT DEFAULT NULL COMMENT '骑手ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
  `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
  `pack_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '打包费',
  `coupon_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券折扣',
  `final_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
  `receiver_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '收货地址经度',
  `receiver_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '收货地址纬度',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
  `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付，1-待接单，2-已接单，3-配送中，4-已完成，5-已取消',
  `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '开始配送时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单明细表
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `food_id` BIGINT NOT NULL COMMENT '餐品ID',
  `food_name` VARCHAR(100) NOT NULL COMMENT '餐品名称',
  `food_image` VARCHAR(255) DEFAULT NULL COMMENT '餐品图片',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT NOT NULL COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_food_id` (`food_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 订单状态日志表
CREATE TABLE `order_status_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `status` TINYINT NOT NULL COMMENT '订单状态',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态日志表';

-- ========== 配送相关表 ==========

-- 骑手表
CREATE TABLE `rider` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '骑手ID',
  `rider_name` VARCHAR(50) NOT NULL COMMENT '骑手姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-休息，1-待接单，2-配送中',
  `current_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '当前位置经度',
  `current_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '当前位置纬度',
  `total_orders` INT DEFAULT 0 COMMENT '总配送订单数',
  `rating` DECIMAL(3,2) DEFAULT 5.00 COMMENT '评分',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手表';

-- 骑手位置记录表
CREATE TABLE `rider_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `rider_id` BIGINT NOT NULL COMMENT '骑手ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
  `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手位置记录表';

-- 配送任务表
CREATE TABLE `delivery_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `rider_id` BIGINT NOT NULL COMMENT '骑手ID',
  `pickup_longitude` DECIMAL(10,7) NOT NULL COMMENT '取餐地点经度',
  `pickup_latitude` DECIMAL(10,7) NOT NULL COMMENT '取餐地点纬度',
  `delivery_longitude` DECIMAL(10,7) NOT NULL COMMENT '送达地点经度',
  `delivery_latitude` DECIMAL(10,7) NOT NULL COMMENT '送达地点纬度',
  `route_data` TEXT DEFAULT NULL COMMENT '路线数据（JSON格式）',
  `current_position` INT DEFAULT 0 COMMENT '当前路线点位置',
  `estimated_time` INT DEFAULT NULL COMMENT '预计送达时间（分钟）',
  `total_distance` INT DEFAULT NULL COMMENT '总距离（米）',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待取餐，1-配送中，2-已送达',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配送任务表';

-- ========== 优惠券相关表 ==========

-- 优惠券表
CREATE TABLE `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `title` VARCHAR(100) NOT NULL COMMENT '优惠券标题',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
  `min_spend` DECIMAL(10,2) DEFAULT NULL COMMENT '最低消费金额',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `total_count` INT DEFAULT 0 COMMENT '发放总量',
  `remain_count` INT DEFAULT 0 COMMENT '剩余数量',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID（null为平台券）',
  `category_ids` VARCHAR(255) DEFAULT NULL COMMENT '适用分类ID（逗号分隔）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE `user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '使用的订单ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- ========== 评价相关表 ==========

-- 评价表
CREATE TABLE `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `rating` TINYINT NOT NULL COMMENT '评分：1-5星',
  `taste_rating` TINYINT DEFAULT NULL COMMENT '口味评分：1-5星',
  `portion_rating` TINYINT DEFAULT NULL COMMENT '分量评分：1-5星',
  `content` TEXT DEFAULT NULL COMMENT '评价内容',
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名',
  `status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过, 2-已拒绝',
  `reply_content` TEXT DEFAULT NULL COMMENT '商家回复内容',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 评价图片表
CREATE TABLE `review_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `review_id` BIGINT NOT NULL COMMENT '评价ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价图片表';

-- ========== 系统相关表 ==========

-- 管理员表
CREATE TABLE `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(64) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 系统公告表
CREATE TABLE `system_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `notice_type` TINYINT DEFAULT 0 COMMENT '公告类型：0-系统通知，1-活动通知，2-维护通知',
  `target_type` TINYINT DEFAULT 0 COMMENT '目标对象：0-全部，1-用户，2-商家',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下线，1-发布',
  `publisher_id` BIGINT DEFAULT NULL COMMENT '发布者ID',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_target_type` (`target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 聊天消息表
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
  `from_user_type` TINYINT NOT NULL COMMENT '发送者类型：1-用户，2-商家，3-客服',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `to_user_type` TINYINT NOT NULL COMMENT '接收者类型：1-用户，2-商家，3-客服',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `content_type` TINYINT DEFAULT 0 COMMENT '内容类型：0-文本，1-图片',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user` (`from_user_id`, `from_user_type`),
  KEY `idx_to_user` (`to_user_id`, `to_user_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 购物车表
CREATE TABLE `cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `merchant_id` BIGINT NOT NULL,
  `food_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `price` DECIMAL(10,2) DEFAULT NULL,
  `food_name` VARCHAR(255) DEFAULT NULL,
  `food_image` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_food_id` (`food_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项表';

-- 用户收藏表
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_merchant` (`user_id`, `merchant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 餐品收藏表
CREATE TABLE `food_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `food_id` BIGINT NOT NULL COMMENT '餐品ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_food` (`user_id`, `food_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_food_id` (`food_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐品收藏表';

-- 通知表
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `type` TINYINT DEFAULT 0 COMMENT '类型：0-系统，1-订单',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';