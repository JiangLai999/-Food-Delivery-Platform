<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-backdrop"></div>
    
    <!-- 品牌水印 -->
    <div class="brand-watermark">
      <img src="/src/assets/logo.png" alt="秒送餐" />
    </div>
    
    <!-- 右上角品牌标语 -->
    <div class="header-brand-tagline">
      <img src="/src/assets/logo.png" alt="秒送餐" class="header-logo-icon" />
      <span>秒送餐 · 智能管理后台</span>
    </div>
    
    <!-- 登录卡片 -->
    <el-card class="login-card">
      <!-- 品牌文化展示 -->
      <div class="brand-culture-show" v-if="!showCultureRead">
        <button class="close-culture-btn" @click="showCultureRead = true">
          <el-icon><Close /></el-icon>
        </button>
        <div class="culture-content">
          <h3>🌟 品牌文化</h3>
          <p><strong>愿景：</strong>成为最受信赖的即时配送平台</p>
          <p><strong>使命：</strong>用专业守护美味，用速度传递温暖</p>
          <p class="culture-highlight">快人一步，美味即达</p>
        </div>
      </div>
      
      <!-- Logo区域 -->
      <div class="login-logo-section">
        <img src="/src/assets/logo.png" alt="秒送餐" class="brand-logo-img" />
        <h2 class="login-title">秒送餐管理后台</h2>
        <p class="login-subtitle">高效管理 · 智能运营 · 专业服务</p>
        <div class="login-tagline">
          <span>💫</span>
          <span>专业的即时配送品牌</span>
          <span>💫</span>
        </div>
      </div>
      
      <el-tabs v-model="activeTab" v-if="showTabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                class="brand-input-wrapper brand-input"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                class="brand-input-wrapper brand-input"
                size="large"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                @click="handleLogin"
                class="brand-button-gradient login-button"
                size="large"
              >
                <span v-if="!loading">登录后台</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
            <div class="forgot-link">
              <el-link type="primary" @click="showTabs = false; activeTab = 'forgot'" class="brand-link">
                <el-icon><Key /></el-icon> 忘记密码
              </el-link>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 忘记密码表单 -->
      <div v-else class="forgot-form">
        <div class="form-header">
          <h3 class="form-title">{{ step === 1 ? '找回密码' : '重置密码' }}</h3>
          <div class="form-icon">
            <el-icon v-if="step === 1"><Key /></el-icon>
            <el-icon v-else><Lock /></el-icon>
          </div>
          <p class="form-subtitle">重置您的管理账户密码</p>
        </div>
        <el-form v-if="step === 1" :model="forgotForm" :rules="forgotRules" ref="forgotFormRef">
          <el-form-item prop="username">
            <el-input
              v-model="forgotForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              class="brand-input-wrapper brand-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSendCode" :loading="sending" class="brand-button" style="width: 100%">
              <span v-if="!sending">发送验证码</span>
              <span v-else>发送中...</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <el-form v-else :model="resetForm" :rules="resetRules" ref="resetFormRef">
          <el-form-item prop="code">
            <el-input
              v-model="resetForm.code"
              placeholder="请输入验证码"
              :prefix-icon="Lock"
              class="brand-input-wrapper brand-input"
            />
          </el-form-item>
          <el-form-item prop="newPassword">
            <el-input
              v-model="resetForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              :prefix-icon="Lock"
              show-password
              class="brand-input-wrapper brand-input"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="resetForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              :prefix-icon="Lock"
              show-password
              class="brand-input-wrapper brand-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleResetPassword" :loading="resetting" class="brand-button" style="width: 100%">
              <span v-if="!resetting">重置密码</span>
              <span v-else>重置中...</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="back-link">
          <el-link type="primary" @click="showTabs = true; step = 1" class="brand-link">
            <el-icon><ArrowLeft /></el-icon> 返回登录
          </el-link>
        </div>
      </div>
    </el-card>
    
    <!-- 品牌标语 -->
    <div class="brand-footer-text">
      <div class="brand-footer-logo">
        <img src="/src/assets/logo.png" alt="秒送餐" />
        <span>秒送餐</span>
      </div>
      <p>快人一步，美味即达</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store'
import request from '../utils/request'
import { User, Lock, Key, ArrowLeft, Close } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const showTabs = ref(true)
const showCultureRead = ref(false)
const step = ref(1)
const loading = ref(false)
const sending = ref(false)
const resetting = ref(false)

const loginFormRef = ref(null)
const forgotFormRef = ref(null)
const resetFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const forgotForm = reactive({
  username: ''
})

const resetForm = reactive({
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const forgotRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const resetRules = {
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    const response = await request({
      url: '/admin/login',
      method: 'post',
      data: loginForm
    })

    userStore.setToken(response.token)
    userStore.setUserInfo({
      username: loginForm.username,
      ...response
    })

    ElMessage.success('登录成功！欢迎回来')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名和密码')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSendCode = async () => {
  if (!forgotFormRef.value) return
  await forgotFormRef.value.validate(async (valid) => {
    if (valid) {
      sending.value = true
      try {
        const response = await request({
          url: '/admin/send-code',
          method: 'post',
          params: {
            username: forgotForm.username
          }
        })
        
        ElMessageBox.alert(
          `您的验证码是：${response}`,
          '验证码已发送',
          {
            confirmButtonText: '确定',
            type: 'success'
          }
        )
        
        ElMessage.success('验证码已获取')
        step.value = 2
      } catch (error) {
        ElMessage.error('发送验证码失败')
        console.error('发送验证码错误:', error)
      } finally {
        sending.value = false
      }
    }
  })
}

const handleResetPassword = async () => {
  const valid = await resetFormRef.value.validate()
  if (!valid) return

  resetting.value = true
  try {
    await request({
      url: '/admin/reset-password',
      method: 'post',
      params: {
        username: forgotForm.username,
        code: resetForm.code,
        newPassword: resetForm.newPassword
      }
    })
    ElMessage.success('密码重置成功，请登录')
    showTabs.value = true
    step.value = 1
    loginForm.password = ''
  } catch (error) {
    ElMessage.error('重置失败')
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f172a 100%);
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* 背景装饰 */
.login-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='100%25' height='100%25' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3Cpattern id='grid' width='50' height='50' patternUnits='userSpaceOnUse'%3E%3Cpath d='M 50 0 L 0 0 0 50' fill='none' stroke='rgba(245, 158, 11, 0.03)' stroke-width='1'/%3E/pattern%3E%3C/defs%3E%3Crect width='100%25' height='100%25' fill='url(%23grid)'/%3E/svg%3E");
  opacity: 0.5;
  pointer-events: none;
}

/* 品牌水印 */
.brand-watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 0;
  pointer-events: none;
  opacity: 0.02;
}

.brand-watermark img {
  width: 600px;
  height: 600px;
  border-radius: 50%;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.15) 0%, transparent 70%);
  top: -200px;
  right: -200px;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.1) 0%, transparent 70%);
  bottom: -100px;
  left: -100px;
}

/* 右上角品牌标语 */
.header-brand-tagline {
  position: fixed;
  top: 20px;
  right: 24px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 12px 20px;
  border-radius: 16px;
  border: 1px solid rgba(245, 158, 11, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo-icon {
  width: 32px;
  height: 32px;
}

.header-brand-tagline span {
  font-size: 14px;
  font-weight: 600;
  color: #F59E0B;
}

/* 登录卡片 */
.login-card {
  width: 440px;
  padding: 48px 40px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, 
    #F59E0B 0%, 
    #D69E2E 50%, 
    #B7791F 100%);
}

/* 品牌文化展示 */
.brand-culture-show {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 12px;
  padding: 16px 20px;
  max-width: 320px;
  z-index: 10;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.close-culture-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(0, 0, 0, 0.4);
  transition: all 0.2s ease;
}

.close-culture-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.6);
}

.culture-content h3 {
  font-size: 16px;
  font-weight: 700;
  color: #F59E0B;
  margin: 0 0 8px 0;
}

.culture-content p {
  font-size: 13px;
  color: #4A5568;
  margin: 4px 0;
  line-height: 1.5;
}

.culture-content strong {
  color: #1A202C;
  font-weight: 600;
}

.culture-highlight {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(245, 158, 11, 0.2);
  color: #F59E0B;
  font-weight: 600;
}

/* Logo区域 */
.login-logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.brand-logo-img {
  width: 100px;
  height: 100px;
  margin-bottom: 16px;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.4);
}

.login-title {
  text-align: center;
  margin: 0 0 8px 0;
  color: #1A202C;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.login-subtitle {
  text-align: center;
  margin: 0 0 16px 0;
  color: #718096;
  font-size: 14px;
  font-weight: 500;
}

.login-tagline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 12px;
  color: #F59E0B;
  font-weight: 600;
}

.login-form {
  margin-top: 24px;
}

.forgot-link {
  text-align: right;
  margin-top: -10px;
}

.forgot-link .el-link {
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.forgot-link .el-link:hover {
  color: #D69E2E;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
}

.brand-button {
  background: linear-gradient(135deg, #F59E0B 0%, #D69E2E 100%);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.3);
  font-weight: 600;
}

.brand-button:hover {
  box-shadow: 0 6px 20px rgba(245, 158, 11, 0.4);
}

/* 忘记密码表单 */
.forgot-form {
  padding: 20px 0;
}

.form-header {
  text-align: center;
  margin-bottom: 24px;
}

.form-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #F59E0B 0%, #D69E2E 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #FFFFFF;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
  margin: 0 auto 12px;
}

.form-title {
  font-size: 20px;
  font-weight: 700;
  color: #1A202C;
  margin: 0;
}

.form-subtitle {
  font-size: 13px;
  color: #718096;
  margin: 0;
}

.back-link .el-link {
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.back-link .el-link:hover {
  color: #D69E2E;
}

.brand-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
}

/* 品牌页脚标语 */
.brand-footer-text {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  z-index: 10;
  opacity: 0.6;
}

.brand-footer-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
}

.brand-footer-logo img {
  width: 32px;
  height: 32px;
  border-radius: 8px;
}

.brand-footer-logo span {
  font-size: 16px;
  font-weight: 700;
  color: #F59E0B;
}

.brand-footer-text p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-weight: 500;
}
</style>
