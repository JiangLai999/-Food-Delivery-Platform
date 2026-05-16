# FoodDelivery - 全栈外卖配送平台 / Full-Stack Food Delivery Platform

---

## 项目简介 / Project Overview

FoodDelivery 是一款功能完整的**全栈外卖配送平台**，具备实时订单追踪、智能推荐算法和多角色用户管理功能。系统包含三个主要组件：Android 用户端、Vue 3 商家管理端和 Vue 3 管理员后台。

FoodDelivery is a production-ready full-stack food delivery platform featuring real-time order tracking, intelligent recommendations, and multi-role user management. The system consists of three main components: Android customer app, Vue 3 merchant portal, and Vue 3 admin dashboard.

---

## 技术栈 / Tech Stack

### 后端 / Backend
| 技术 / Technology | 版本 / Version |
|-------------------|----------------|
| Spring Boot | 2.7.18 |
| Java | 11 |
| MySQL | 8.0 |
| MyBatis-Plus | 3.5.3.1 |
| JWT Authentication | - |
| WebSocket (STOMP) | - |

### 前端 / Frontend
| 组件 / Component | 技术 / Technology |
|-----------------|-------------------|
| Android 用户端 / Android App | Kotlin + Jetpack Compose |
| Web 商家端 / Merchant Portal | Vue 3 + Element Plus + ECharts + Vite |
| Web 管理端 / Admin Dashboard | Vue 3 + Element Plus + ECharts + Vite |

---

## 项目结构 / Project Structure

```
FoodDeliverySystem/
├── android-user/        # Android 用户端 (Kotlin + Compose)
│   └── app/src/main/java/com/fooddelivery/user/
├── backend/             # Spring Boot 后端服务
│   └── src/main/java/com/fooddelivery/
├── web-merchant/        # 商家管理端 (Vue 3)
└── web-admin/           # 管理员后台 (Vue 3)
```

---

## 快速启动 / Quick Start

### 环境要求 / Prerequisites
- JDK 11+
- Node.js 16+
- MySQL 8.0
- Android SDK（Android 应用开发）

### 1. 数据库初始化 / Database Setup

```sql
CREATE DATABASE food_delivery DEFAULT CHARACTER SET utf8mb4;
```

从 `backend/src/main/resources/sql/` 导入数据库schema。

Import schema from `backend/src/main/resources/sql/`.

### 2. 启动后端 / Start Backend

```bash
cd backend
mvn clean package -DskipTests
java -jar target/food-delivery-backend-1.0.0.jar
```

- API 地址 / API: http://localhost:8080/api
- Swagger 文档 / Swagger UI: http://localhost:8080/swagger-ui/index.html

### 3. 启动商家端 / Start Merchant Portal

```bash
cd web-merchant
npm install
npm run dev
# 访问 / Access: http://localhost:3001
```

### 4. 启动管理端 / Start Admin Dashboard

```bash
cd web-admin
npm install
npm run dev
# 访问 / Access: http://localhost:3000
```

### 5. 编译 Android 应用 / Build Android App

```bash
cd android-user
./gradlew assembleDebug
# APK 输出 / Output: android-user/app/build/outputs/apk/debug/app-debug.apk
```

---

## 默认账号 / Default Accounts

| 角色 / Role | 用户名 / Username | 密码 / Password |
|-------------|------------------|-----------------|
| 管理员 / Admin | admin | 88888888 |
| 用户 / User | 13900000001 | 123456 |
| 商家 / Merchant | 13800138001 | 123456 |

---

## 功能特性 / Features

### 用户端 Android 应用 / Customer App (Android)
- ✅ 用户注册登录 / User registration & authentication
- ✅ 商家餐品浏览与搜索 / Restaurant & menu browsing with search
- ✅ 智能推荐算法 / Smart recommendations (collaborative filtering)
- ✅ 购物车管理 / Shopping cart management
- ✅ 订单创建与支付 / Order placement & payment
- ✅ 订单实时跟踪 / Real-time delivery tracking
- ✅ 订单评价 / Order reviews & ratings
- ✅ 地址管理 / Address management
- ✅ 优惠券使用 / Coupon system
- ✅ 系统公告 / System announcements

### 商家端 Web 门户 / Merchant Portal (Web)
- ✅ 商家登录注册 / Merchant authentication
- ✅ 餐品管理（增删改查）/ Menu & category management
- ✅ 订单处理（接单/拒单）/ Order processing (accept/reject)
- ✅ 订单实时跟踪 / Real-time order tracking
- ✅ 评价管理（查看/回复）/ Review management & responses
- ✅ 数据统计（ECharts图表）/ Analytics dashboard (ECharts)
- ✅ 营业设置 / Business hours configuration

### 管理端 Web 后台 / Admin Dashboard (Web)
- ✅ 管理员登录 / Admin authentication
- ✅ 商家审核管理 / Merchant approval workflow
- ✅ 用户管理 / User management
- ✅ 内容审核（评价审核）/ Content moderation (reviews)
- ✅ 分类管理 / Category management
- ✅ 系统公告发布 / Announcement publishing
- ✅ 数据统计展示 / Analytics & reporting
- ✅ 数据库备份 / Database backup

---

## 技术亮点 / Key Technical Highlights

### 实时配送追踪 / Real-time Delivery Tracking
系统通过定时任务模拟骑手移动，基于高德地图API规划配送路径，实现类似美团的实时配送跟踪效果。

Simulated rider movement with route planning via Gaode (Amap) Maps API, delivering real-time location updates via WebSocket.

### 协同过滤推荐算法 / Collaborative Filtering Recommendations
基于用户历史订单数据，计算用户相似度，智能推荐相似用户喜欢的商家和餐品。

Analyzes user order history to calculate user similarity and recommend personalized restaurants and menu items.

### WebSocket 实时通信 / WebSocket Real-time Communication
订单状态变化和骑手位置通过 WebSocket 实时推送给用户端。

Pushes order status updates and rider locations to the Android app in real-time.

### ECharts 数据可视化 / ECharts Data Visualization
商家端和管理端集成 ECharts 图表，展示订单量、销售额、热销商品等数据统计。

Interactive charts displaying order volume, revenue, popular items, and business metrics.

---

## 环境变量 / Environment Variables

### 后端配置 / Backend (application.yml)
- `server.port` - 服务端口（默认8080）/ Server port (default: 8080)
- `spring.datasource` - 数据库连接 / Database connection
- `jwt.secret` - JWT签名密钥 / JWT signing key
- `amap.key` - 高德地图API密钥 / Gaode Maps API key

### 前端配置 / Web Frontends (.env)
- `VITE_API_BASE_URL` - 后端API地址 / Backend API base URL

---

## API 文档 / API Documentation

后端运行时，访问 `/swagger-ui/index.html` 获取完整API文档。

Full API documentation available at `/swagger-ui/index.html` when backend is running.

---

## 许可证 / License

MIT License