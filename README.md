# FoodDelivery - 全栈外卖配送平台 / Full-Stack Food Delivery Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-11-orange)](https://www.oracle.com/java/)
[![Vue](https://img.shields.io/badge/Vue-3-green)](https://vuejs.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-purple)](https://developer.android.com/compose)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-red)](https://spring.io/projects/spring-websocket)

---

## 项目简介 / Project Overview

FoodDelivery 是一款功能完整的**全栈外卖配送平台**，具备实时订单追踪、智能推荐算法和多角色用户管理功能。系统包含三个主要组件：Android 用户端、Vue 3 商家管理端和 Vue 3 管理员后台。

FoodDelivery is a production-ready full-stack food delivery platform featuring real-time order tracking, intelligent recommendations, and multi-role user management. The system consists of three main components: Android customer app, Vue 3 merchant portal, and Vue 3 admin dashboard.

---

## 系统架构 / System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端层 / Client Layer                   │
├──────────────┬─────────────────────────┬────────────────────────┤
│   Android    │    Web Merchant Portal  │    Web Admin Dashboard  │
│    User      │        :3001            │        :3000            │
│   App        │                         │                        │
└──────┬───────┴───────────┬─────────────┴────────────┬─────────────┘
       │                  │                          │
       │    HTTP/REST     │     HTTP/REST            │
       │    WebSocket     │                          │
       ▼                  ▼                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      网关层 / API Gateway Layer                  │
│                   Spring Boot Backend :8080                      │
├──────────┬──────────┬──────────┬──────────┬──────────┬────────────┤
│   User   │  Order   │ Merchant│  Food    │ Delivery │  Review    │
│   API    │   API    │   API   │   API    │   API    │    API     │
└──────────┴──────────┴──────────┴──────────┴──────────┴────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      数据层 / Data Layer                         │
│                    MySQL 8.0 Database                           │
│                  ┌───────────────────────┐                      │
│                  │   food_delivery       │                      │
│                  └───────────────────────┘                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 技术栈 / Tech Stack

### 后端 / Backend
| 技术 / Technology | 版本 / Version | 说明 / Description |
|-------------------|----------------|-------------------|
| Spring Boot | 2.7.18 | 核心框架 / Core framework |
| Java | 11 | 编程语言 / Programming language |
| MySQL | 8.0 | 关系型数据库 / Relational database |
| MyBatis-Plus | 3.5.3.1 | ORM框架 / ORM framework |
| JWT | - | 身份认证 / Authentication |
| WebSocket | STOMP | 实时通信 / Real-time communication |
| Lombok | - | 简化代码 / Code simplification |

### 前端 / Frontend
| 组件 / Component | 技术 / Technology | 说明 / Description |
|-----------------|-------------------|-------------------|
| Android 用户端 / Android App | Kotlin + Jetpack Compose | 顾客移动应用 |
| Web 商家端 / Merchant Portal | Vue 3 + Element Plus + ECharts + Vite | 商家管理 |
| Web 管理端 / Admin Dashboard | Vue 3 + Element Plus + ECharts + Vite | 系统管理 |

### 基础设施 / Infrastructure
| 组件 / Component | 用途 / Purpose |
|-----------------|----------------|
| 高德地图 API / Gaode Maps API | 地理编码、路径规划 |
| ECharts | 数据可视化图表 |
| Maven | 后端依赖管理 |
| npm | 前端依赖管理 |
| Gradle | Android 构建工具 |

---

## 项目结构 / Project Structure

```
FoodDeliverySystem/
├── android-user/                     # Android 用户端
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/fooddelivery/user/
│   │       │   ├── model/            # 数据模型
│   │       │   ├── network/          # 网络请求 (Retrofit)
│   │       │   ├── ui/compose/       # Compose 页面
│   │       │   ├── utils/            # 工具类
│   │       │   └── websocket/        # WebSocket 管理
│   │       └── res/                   # 资源文件
│   ├── gradle/                        # Gradle 配置
│   └── build.gradle
│
├── backend/                           # Spring Boot 后端
│   └── src/main/
│       ├── java/com/fooddelivery/
│       │   ├── config/               # 配置类
│       │   ├── controller/           # 控制器 (~30个)
│       │   ├── dto/                  # 数据传输对象
│       │   ├── entity/               # 实体类
│       │   ├── mapper/               # MyBatis Mapper
│       │   ├── scheduler/            # 定时任务
│       │   ├── service/              # 服务接口
│       │   ├── service/impl/        # 服务实现
│       │   ├── utils/                # 工具类
│       │   ├── vo/                   # 视图对象
│       │   └── websocket/            # WebSocket 服务
│       └── resources/
│           ├── application.yml      # 应用配置
│           └── sql/                  # SQL 脚本
│
├── web-merchant/                      # 商家端 Web
│   └── src/
│       ├── api/                      # API 调用
│       ├── components/               # 公共组件
│       ├── router/                   # 路由配置
│       ├── store/                    # 状态管理
│       ├── utils/                    # 工具类
│       └── views/                    # 页面组件
│
└── web-admin/                         # 管理端 Web
    └── src/
        ├── api/                      # API 调用
        ├── components/               # 公共组件
        ├── router/                   # 路由配置
        ├── store/                    # 状态管理
        ├── styles/                   # 样式文件
        └── views/                    # 页面组件
```

---

## 数据库设计 / Database Schema

### 核心表结构 / Core Tables

| 表名 / Table | 说明 / Description | 主键 / PK |
|-------------|-------------------|-----------|
| `user` | 用户信息 / User info | id |
| `merchant` | 商家信息 / Merchant info | id |
| `food_item` | 餐品信息 / Food items | id |
| `food_category` | 餐品分类 / Food categories | id |
| `orders` | 订单主表 / Order header | id |
| `order_item` | 订单明细 / Order items | id |
| `cart_item` | 购物车 / Shopping cart | id |
| `review` | 评价 / Reviews | id |
| `coupon` | 优惠券 / Coupons | id |
| `user_coupon` | 用户优惠券 / User coupons | id |
| `rider` | 骑手 / Riders | id |
| `delivery_task` | 配送任务 / Delivery tasks | id |
| `user_address` | 用户地址 / User addresses | id |
| `user_favorite` | 收藏夹 / Favorites | id |
| `system_notice` | 系统公告 / System notices | id |
| `admin_notice` | 管理员公告 / Admin notices | id |

### ER 关系图 / Entity Relationships

```
User 1 ─────< Order (1:N)
User 1 ─────< CartItem (1:N)
User 1 ─────< UserAddress (1:N)
User 1 ─────< UserCoupon (1:N)
User 1 ─────< Review (1:N)
User 1 ─────< UserFavorite (1:N)

Merchant 1 ─────< FoodItem (1:N)
Merchant 1 ─────< Review (1:N)
Merchant 1 ─────< FoodCategory (1:N)

Order 1 ─────< OrderItem (1:N)
Order 1 ─────< DeliveryTask (1:1)
Order 1 ─────< Review (1:1)

Rider 1 ─────< DeliveryTask (1:N)
```

---

## 快速启动 / Quick Start

### 环境要求 / Prerequisites

| 环境 / Environment | 版本 / Version | 说明 / Description |
|-------------------|----------------|-------------------|
| JDK | 11+ | 后端运行环境 |
| Node.js | 16+ | 前端运行环境 |
| MySQL | 8.0 | 数据库 |
| Android SDK | 最新版 / Latest | Android 开发 |
| Maven | 3.6+ | 后端构建 |

### 1. 数据库初始化 / Database Setup

```sql
-- 创建数据库
CREATE DATABASE food_delivery DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE food_delivery;

-- 导入 SQL 脚本 (按顺序执行)
-- 1. schema_no_data.sql - 创建表结构
-- 2. init_data.sql - 初始化基础数据
-- 3. data.sql - 导入测试数据
```

### 2. 后端配置 / Backend Configuration

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_delivery?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的密码

jwt:
  secret: your_jwt_secret_key  # 修改为你的 JWT 密钥
  expiration: 86400000

amap:
  key: your_amap_api_key  # 高德地图 API Key
```

### 3. 启动后端 / Start Backend

```bash
# 进入后端目录
cd backend

# 编译打包
mvn clean package -DskipTests

# 启动服务
java -jar target/food-delivery-backend-1.0.0.jar

# 或使用脚本启动
./start-backend.sh  # Linux/Mac
start-backend.bat   # Windows
```

服务地址:
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### 4. 启动商家端 / Start Merchant Portal

```bash
cd web-merchant

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

访问地址: http://localhost:3001

### 5. 启动管理端 / Start Admin Dashboard

```bash
cd web-admin

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

访问地址: http://localhost:3000

### 6. Android 应用 / Android App

```bash
cd android-user

# 配置 API 地址 (可选)
# 编辑 app/src/main/java/.../utils/AppConfig.kt

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 默认账号 / Default Accounts

| 角色 / Role | 用户名 / Username | 密码 / Password | 说明 / Description |
|-------------|------------------|-----------------|-------------------|
| 管理员 / Admin | admin | 88888888 | 系统管理员，拥有所有权限 |
| 用户 / User | 13900000001 | 123456 | 测试用户，可下单、评价等 |
| 商家 / Merchant | 13800138001 | 123456 | 测试商家，可管理菜品、订单 |

---

## 功能特性 / Features

### 用户端 Android 应用 / Customer App (Android)

| 功能 / Feature | 说明 / Description | 状态 / Status |
|---------------|-------------------|---------------|
| 用户注册登录 | 手机号注册、密码登录、JWT认证 | ✅ |
| 商家浏览 | 首页推荐、分类筛选、搜索 | ✅ |
| 智能推荐 | 协同过滤算法，个性化推荐 | ✅ |
| 餐品详情 | 图片、价格、评价、规格 | ✅ |
| 购物车 | 添加、删除、修改数量 | ✅ |
| 下单支付 | 支持余额、支付宝、微信 | ✅ |
| 订单列表 | 待支付/待接单/配送中/已完成 | ✅ |
| 实时追踪 | WebSocket 推送骑手位置 | ✅ |
| 订单评价 | 星级+文字+图片评价 | ✅ |
| 地址管理 | 新增、编辑、删除收货地址 | ✅ |
| 优惠券 | 领取、使用优惠券 | ✅ |
| 收藏夹 | 收藏/取消收藏商家 | ✅ |
| 充值 | 余额充值功能 | ✅ |
| 客服 | 在线客服咨询 | ✅ |
| 公告 | 系统公告查看 | ✅ |

### 商家端 Web 门户 / Merchant Portal (Web)

| 功能 / Feature | 说明 / Description | 状态 / Status |
|---------------|-------------------|---------------|
| 商家入驻 | 注册、提交审核 | ✅ |
| 商家登录 | JWT 认证登录 | ✅ |
| 店铺管理 | 店铺信息设置、营业状态 | ✅ |
| 菜品管理 | 添加、编辑、删除菜品 | ✅ |
| 分类管理 | 菜品分类管理 | ✅ |
| 订单处理 | 接单、拒单、取消订单 | ✅ |
| 实时通知 | 新订单 WebSocket 提醒 | ✅ |
| 配送追踪 | 查看骑手配送进度 | ✅ |
| 评价管理 | 查看用户评价、商家回复 | ✅ |
| 数据统计 | ECharts 图表展示经营数据 | ✅ |
| 消息中心 | 用户消息、客服消息 | ✅ |

### 管理端 Web 后台 / Admin Dashboard (Web)

| 功能 / Feature | 说明 / Description | 状态 / Status |
|---------------|-------------------|---------------|
| 管理员登录 | JWT 认证登录 | ✅ |
| 数据概览 | 实时数据统计看板 | ✅ |
| 用户管理 | 用户列表、禁用/启用 | ✅ |
| 商家管理 | 商家审核（通过/拒绝）、管理 | ✅ |
| 评价审核 | 用户评价内容审核 | ✅ |
| 分类管理 | 平台菜品分类管理 | ✅ |
| 公告管理 | 发布、编辑系统公告 | ✅ |
| 消息中心 | 全局消息推送 | ✅ |
| 数据统计 | ECharts 可视化数据展示 | ✅ |
| 数据库备份 | 数据库备份与恢复 | ✅ |
| 系统设置 | 平台参数配置 | ✅ |

---

## 技术亮点 / Key Technical Highlights

### 1. 实时配送追踪 / Real-time Delivery Tracking

**实现方式:**
- 后端定时任务模拟骑手移动 (`RiderLocationScheduler`)
- 高德地图 API 路径规划 (`AmapUtil`)
- WebSocket STOMP 协议实时推送位置
- Android 端接收并展示配送进度

**技术流程:**
```
订单支付成功 → 创建配送任务 → 分配骑手 → 定时更新位置 → WebSocket 推送 → Android 展示
```

### 2. 协同过滤推荐算法 / Collaborative Filtering

**算法原理:**
- 基于用户的协同过滤 (User-based CF)
- 计算用户间的相似度 (余弦相似度)
- 根据相似用户购买记录推荐商品

**实现位置:** `RecommendationServiceImpl.java`

### 3. WebSocket 实时通信 / WebSocket Real-time Communication

**应用场景:**
- 新订单通知 (商家端)
- 订单状态变更 (用户端)
- 骑手位置更新 (用户端)
- 即时消息 (客服咨询)

**协议:** STOMP over WebSocket

### 4. ECharts 数据可视化 / ECharts Visualization

**商家端统计:**
- 今日订单量/销售额
- 本周订单趋势图
- 热销菜品排行
- 收入构成饼图

**管理端统计:**
- 平台总用户/商家/订单
- 订单量趋势
- 商家销量排行
- 用户增长趋势

### 5. JWT 身份认证 / JWT Authentication

- 支持用户、商家、管理员三种角色
- Token 有效期 24 小时
- 刷新 Token 机制
- 拦截器权限验证

### 6. MyBatis-Plus CRUND

- 简化 CRUD 操作
- 分页插件
- 自动填充
- 逻辑删除

---

## API 文档 / API Documentation

### 用户端 API / User API

| 接口 / Endpoint | 方法 / Method | 说明 / Description |
|----------------|---------------|-------------------|
| `/api/user/register` | POST | 用户注册 |
| `/api/user/login` | POST | 用户登录 |
| `/api/user/info` | GET | 获取用户信息 |
| `/api/user/address/list` | GET | 地址列表 |
| `/api/user/address/add` | POST | 添加地址 |
| `/api/user/address/update` | PUT | 更新地址 |
| `/api/user/address/delete/{id}` | DELETE | 删除地址 |

### 商家端 API / Merchant API

| 接口 / Endpoint | 方法 / Method | 说明 / Description |
|----------------|---------------|-------------------|
| `/api/merchant/register` | POST | 商家注册 |
| `/api/merchant/login` | POST | 商家登录 |
| `/api/merchant/info` | GET | 商家信息 |
| `/api/merchant/orders` | GET | 订单列表 |
| `/api/merchant/order/accept/{id}` | POST | 接单 |
| `/api/merchant/order/reject/{id}` | POST | 拒单 |
| `/api/merchant/food/list` | GET | 菜品列表 |
| `/api/merchant/food/add` | POST | 添加菜品 |
| `/api/merchant/food/update` | PUT | 更新菜品 |

### 订单 API / Order API

| 接口 / Endpoint | 方法 / Method | 说明 / Description |
|----------------|---------------|-------------------|
| `/api/order/create` | POST | 创建订单 |
| `/api/order/pay/{id}` | POST | 支付订单 |
| `/api/order/list` | GET | 订单列表 |
| `/api/order/{id}` | GET | 订单详情 |
| `/api/order/cancel/{id}` | POST | 取消订单 |

### 管理端 API / Admin API

| 接口 / Endpoint | 方法 / Method | 说明 / Description |
|----------------|---------------|-------------------|
| `/api/admin/login` | POST | 管理员登录 |
| `/api/admin/merchants` | GET | 商家列表 |
| `/api/admin/merchant/audit/{id}` | POST | 审核商家 |
| `/api/admin/users` | GET | 用户列表 |
| `/api/admin/reviews` | GET | 评价列表 |
| `/api/admin/backup` | POST | 数据库备份 |

完整 API 文档请访问: http://localhost:8080/swagger-ui/index.html

---

## 环境变量 / Environment Variables

### 后端 / Backend (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_delivery
    username: root
    password: password

jwt:
  secret: your-secret-key-min-32-chars
  expiration: 86400000  # 24 hours

amap:
  key: your-amap-api-key

rider:
  simulator:
    enabled: true
    interval: 5000  # 5 seconds
```

### 前端 / Frontend (.env)

```env
# 开发环境
VITE_API_BASE_URL=http://localhost:8080/api

# 生产环境
VITE_API_BASE_URL=https://your-domain.com/api
```

---

## 目录结构说明 / Directory Structure

### backend/ 后端目录

| 目录 / Directory | 说明 / Description |
|-----------------|-------------------|
| `config/` | Spring 配置类 (CORS、JWT、WebSocket等) |
| `controller/` | REST API 控制器 |
| `dto/` | 数据传输对象 |
| `entity/` | 数据库实体类 |
| `mapper/` | MyBatis Mapper 接口 |
| `scheduler/` | 定时任务 (骑手模拟) |
| `service/` | 业务逻辑接口 |
| `service/impl/` | 业务逻辑实现 |
| `utils/` | 工具类 |
| `vo/` | 视图对象 |
| `websocket/` | WebSocket 服务 |

### android-user/ Android 目录

| 目录 / Directory | 说明 / Description |
|-----------------|-------------------|
| `model/` | 数据模型类 |
| `network/` | Retrofit 网络请求 |
| `ui/compose/` | Compose Activity 页面 |
| `ui/compose/screens/` | Compose Screen 页面 |
| `ui/compose/components/` | Compose 组件 |
| `ui/compose/viewmodel/` | ViewModel |
| `utils/` | 工具类 |
| `websocket/` | WebSocket 管理 |

### web-merchant/ & web-admin/ 前端目录

| 目录 / Directory | 说明 / Description |
|-----------------|-------------------|
| `api/` | API 接口调用 |
| `components/` | Vue 公共组件 |
| `router/` | Vue Router 配置 |
| `store/` | Pinia 状态管理 |
| `views/` | 页面组件 |
| `styles/` | 样式文件 |
| `utils/` | 工具函数 |

---

## 常见问题 / FAQ

### Q: 高德地图 API Key 如何获取？
A: 访问 [高德开放平台](https://lbs.amap.com/)，注册账号并创建应用获取 Key。

### Q: 如何配置生产环境？
A:
1. 修改 `application.yml` 中的数据库连接
2. 修改 `.env` 中的 API 地址
3. 使用 `npm run build` 构建前端
4. 打包后端 JAR
5. 部署到服务器

### Q: WebSocket 连接失败？
A: 检查防火墙是否开放 8080 端口，确保后端 WebSocket 配置正确。

### Q: Android 真机调试无法连接？
A: 确保手机和电脑在同一局域网，修改 API 地址为电脑 IP。

### Q: 如何备份数据库？
A: 使用管理后台的备份功能，或手动执行 `mysqldump` 命令。

---

## 更新日志 / Changelog

### v1.0.0 (2026-01)
- 初始版本发布
- 完整用户端功能
- 商家端管理功能
- 管理端后台功能
- 实时配送追踪
- 智能推荐系统

---

## 贡献指南 / Contributing

1. Fork 本项目
2. 创建分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 许可证 / License

本项目基于 [MIT License](https://opensource.org/licenses/MIT) 开源。

---

## 联系方式 / Contact

- 项目主页: https://github.com/JiangLai999/-Food-Delivery-Platform
- 问题反馈: https://github.com/JiangLai999/-Food-Delivery-Platform/issues

---

## 致谢 / Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Jetpack Compose](https://developer.android.com/compose) - Android UI
- [Element Plus](https://element-plus.org/) - UI 组件库
- [ECharts](https://echarts.apache.org/) - 数据可视化
- [高德地图](https://lbs.amap.com/) - 地图服务