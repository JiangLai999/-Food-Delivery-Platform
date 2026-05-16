<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="aside">
      <!-- 品牌Logo -->
      <div class="logo">
        <img src="/src/assets/logo.png" alt="秒送餐" class="logo-img" />
        <div class="logo-text">秒送餐管理</div>
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        :router="true"
        background-color="#1a1a2e"
        text-color="#a0a0b8"
        active-text-color="#F59E0B"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/merchants">
          <el-icon><Shop /></el-icon>
          <span>商家管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/reviews">
          <el-icon><Document /></el-icon>
          <span>内容审核</span>
        </el-menu-item>
        <el-menu-item index="/notices">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/messages">
          <el-icon><Message /></el-icon>
          <span>客服中心</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>

      <!-- 侧边栏底部品牌标语 -->
      <div class="aside-footer">
        <div class="brand-tagline-small">
          <img src="/src/assets/logo.png" alt="秒送餐" class="footer-logo-icon" />
          <div class="tagline-text">
            <span>秒送餐</span>
            <span>智能运营 · 高效管理</span>
          </div>
        </div>
      </div>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <!-- 左侧面包屑 -->
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title" style="color: #F59E0B;">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 右侧品牌元素和用户信息 -->
        <div class="header-right">
          <!-- 品牌标语 -->
          <div class="header-brand-tagline">
            <img src="/src/assets/logo.png" alt="秒送餐" class="header-logo-icon" />
            <span>专业的管理平台</span>
          </div>

          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <div class="avatar-container">
                <el-avatar :size="38">
                  <img v-if="userInfo.avatar" :src="userInfo.avatar" />
                  <span v-else>{{ userInfo.username?.charAt(0) || '管' }}</span>
                </el-avatar>
                <div class="avatar-badge"></div>
              </div>
              <span class="username">{{ userInfo.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled style="color: #F59E0B;">
                  <el-icon><UserFilled /></el-icon>
                  {{ userInfo.username || '管理员' }}
                </el-dropdown-item>
                <el-dropdown-item command="settings">账户设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />

        <!-- 品牌页脚 -->
        <footer class="brand-footer-layout">
          <div class="footer-logo">
            <img src="/src/assets/logo.png" alt="秒送餐" />
            <div class="footer-brand-info">
              <span class="footer-brand-name">秒送餐管理</span>
              <span class="footer-brand-slogan">快人一步，美味即达</span>
            </div>
          </div>
          <div class="footer-divider"></div>
          <div class="footer-info">
            <p>专业的即时配送品牌，智能运营，高效管理</p>
            <p>© 2026 秒送餐 版权所有</p>
          </div>
        </footer>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store'
import { ElMessageBox } from 'element-plus'
import { DataAnalysis, User, Shop, Menu, Bell, Setting, ArrowDown, Document, Message, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)
const userInfo = computed(() => userStore.userInfo)

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.clearAuth()
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'settings') {
    router.push('/settings')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: var(--bg-subtle-gradient);
}

/* 侧边栏 */
.aside {
  background-color: #1a1a2e;
  background-image: linear-gradient(180deg, #1a1a2e 0%, #16162a 100%);
  overflow: hidden;
  position: relative;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.08);
}

/* 品牌Logo */
.logo {
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 0;
  border-bottom: 2px solid transparent;
  border-image: linear-gradient(90deg,
    transparent 0%,
    #F59E0B 20%,
    #D69E2E 50%,
    #F59E0B 80%,
    transparent 100%) 1;
}

.logo-img {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.4);
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.el-menu-vertical {
  border: none;
  padding: 0 12px 0;
}

.el-menu-vertical .el-menu-item {
  height: 52px;
  line-height: 52px;
  border-radius: var(--border-radius-sm);
  margin: 4px 0;
  transition: all 0.3s ease;
}

.el-menu-vertical .el-menu-item .el-icon {
  font-size: 18px;
}

.el-menu-vertical .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(245, 158, 11, 0.15) 0%, rgba(245, 158, 11, 0.05) 100%) !important;
  border: 1px solid rgba(245, 158, 11, 0.3);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.2);
}

.el-menu-vertical .el-menu-item:hover:not(.is-active) {
  background: rgba(255, 255, 255, 0.05);
}

/* 侧边栏底部品牌标语 */
.aside-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(0, 0, 0, 0.2);
}

.brand-tagline-small {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: var(--border-radius-sm);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.brand-tagline-small span:first-child {
  font-size: 14px;
  font-weight: 700;
  color: #D69E2E;
}

.brand-tagline-small span:last-child {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.footer-logo-icon {
  width: 20px;
  height: 20px;
}

.tagline-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* 顶部栏 */
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  border-bottom: 2px solid transparent;
  border-image: linear-gradient(90deg,
    transparent 0%,
    rgba(245, 158, 11, 0.1) 50%,
    transparent 100%) 1;
}

.header-left {
  font-size: 14px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 顶部栏品牌标语 */
.header-brand-tagline {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFFFFF 100%);
  border-radius: var(--border-radius-lg);
  border: 1px solid rgba(245, 158, 11, 0.15);
  box-shadow: var(--shadow-soft);
}

.header-logo-icon {
  width: 24px;
  height: 24px;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--border-radius-md);
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #FFFFFF;
  box-shadow: var(--shadow-soft);
  transform: translateY(-2px);
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #52C41A;
  border: 2px solid #FFFFFF;
  border-radius: 50%;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-dark);
}

/* 主内容区 */
.main {
  background: transparent;
  padding: 24px;
  overflow-y: auto;
}

/* 品牌页脚 */
.brand-footer-layout {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-top: 1px solid rgba(245, 158, 11, 0.2);
  margin-top: 40px;
  padding: 24px;
  border-radius: var(--border-radius-lg);
  position: relative;
  left: 0;
  right: 0;
}

.footer-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.footer-logo img {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  opacity: 0.9;
}

.footer-brand-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.footer-brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #F59E0B;
}

.footer-brand-slogan {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.footer-divider {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #F59E0B 0%, #D69E2E 50%, #B7791F 100%);
  margin: 20px auto;
  opacity: 0.6;
}

.footer-info {
  text-align: center;
}

.footer-info p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin: 4px 0;
  line-height: 1.6;
}
</style>
