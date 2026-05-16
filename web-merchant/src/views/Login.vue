<template>
  <div class="login-container">
    <!-- 背景遮罩 -->
    <div class="login-backdrop"></div>
    
    <!-- 品牌水印 -->
    <div class="brand-watermark">
      <img src="/src/assets/logo.png" alt="秒送餐" />
    </div>
    
    <!-- 右上角品牌标语 -->
    <div class="brand-tagline">
      <img src="/src/assets/logo.png" alt="秒送餐" class="header-logo-icon" />
      <div class="text">秒送餐 · 商家平台</div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 品牌文化展示 -->
      <div class="brand-culture-show" v-if="!showCultureRead">
        <button class="close-culture-btn" @click="showCultureRead = true">
          <el-icon><Close /></el-icon>
        </button>
        <div class="culture-content">
          <h3>✨ 品牌服务</h3>
          <p><strong>宗旨：</strong>准时送达，美味即达</p>
          <p><strong>承诺：</strong>专业服务，商户第一</p>
          <p class="culture-highlight">快人一步，开通美味事业</p>
        </div>
      </div>
      
      <!-- Logo区域 -->
      <div class="login-logo-section">
        <img src="/src/assets/logo.png" alt="秒送餐" class="brand-logo-img" />
        <div class="login-header">
          <h1>秒送餐商家</h1>
          <p>登录您的商家账号，开启外卖生意</p>
          <div class="tagline">
            <span>🔥</span>
            <span>专业的即时配送平台</span>
            <span>🔥</span>
          </div>
        </div>
      </div>
      
      <el-tabs v-model="activeTab" v-if="showTabs">
        <el-tab-pane label="登录" name="login">
          <el-form ref="formRef" :model="loginForm" :rules="rules" class="login-form">
            <el-form-item prop="phone">
              <el-input 
                v-model="loginForm.phone" 
                placeholder="请输入手机号"
                :prefix-icon="Phone"
                size="large"
                class="custom-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
                class="custom-input"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                :loading="loading" 
                class="login-button"
                @click="handleLogin"
              >
                登录后台
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="login-footer">
            <span>还没有账号？</span>
            <el-link type="primary" :underline="false" @click="router.push('/register')">立即入驻</el-link>
            <span class="separator">|</span>
            <el-link type="primary" :underline="false" @click="showTabs = false; activeTab = 'forgot'">忘记密码？</el-link>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 忘记密码表单 -->
      <div v-else class="forgot-form">
        <div class="form-header">
          <h3 class="form-title">{{ step === 1 ? '忘记密码' : '重置密码' }}</h3>
          <div class="form-icon">
            <el-icon v-if="step === 1"><Key /></el-icon>
            <el-icon v-else><Lock /></el-icon>
          </div>
          <p class="form-subtitle">重置您的商家账号密码</p>
        </div>
        <el-form v-if="step === 1" :model="forgotForm" :rules="forgotRules" ref="forgotFormRef">
          <el-form-item prop="phone">
            <el-input
              v-model="forgotForm.phone"
              placeholder="请输入手机号"
              :prefix-icon="Phone"
              class="custom-input"
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
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="newPassword">
            <el-input
              v-model="resetForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="resetForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
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
    </div>
    
    <!-- 品牌页脚标语 -->
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
import { ElMessage } from 'element-plus'
import { Phone, Lock, Key, ArrowLeft, Close } from '@element-plus/icons-vue'
import { useUserStore } from '../store'
import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const showTabs = ref(true)
const showCultureRead = ref(false)
const step = ref(1)
const loading = ref(false)
const sending = ref(false)
const resetting = ref(false)

const formRef = ref()
const forgotFormRef = ref()
const resetFormRef = ref()

const loginForm = reactive({
  phone: '',
  password: ''
})

const forgotForm = reactive({
  phone: ''
})

const resetForm = reactive({
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const forgotRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
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
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = await request({
          url: '/merchant/login',
          method: 'post',
          data: loginForm
        })
        
        userStore.setToken(data.token)
        userStore.setUserInfo(data.merchant)
        userStore.setMerchantInfo(data.merchant)
        
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleSendCode = async () => {
  if (!forgotFormRef.value) return
  await forgotFormRef.value.validate(async (valid) => {
    if (valid) {
      sending.value = true
      try {
        const response = await request({
          url: '/merchant/send-code',
          method: 'post',
          params: {
            phone: forgotForm.phone
          }
        })
        
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
      url: '/merchant/reset-password',
      method: 'post',
      params: {
        phone: forgotForm.phone,
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
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* 深色背景 + 渐变 */
.login-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='100%25' height='100%25' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3Cpattern id='grid' width='60' height='60' patternUnits='userSpaceOnUse'%3E%3Cpath d='M 60 0 L 0 0 0 60' fill='none' stroke='rgba(255, 140, 0, 0.03)' stroke-width='1.5'/%3E/pattern%3E%3C/defs%3E%3Crect width='100%25' height='100%25' fill='url(%23grid)'/%3E/svg%3E");
  opacity: 0.6;
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

/* 添加装饰性圆形 */
.login-container::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 140, 0, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: -150px;
  left: -150px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 140, 0, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

/* 品牌标语 */
.brand-tagline {
  position: fixed;
  top: 20px;
  right: 24px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 12px 20px;
  border-radius: 16px;
  border: 1px solid rgba(255, 140, 0, 0.2);
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

.brand-tagline .text {
  font-size: 14px;
  font-weight: 600;
  color: #FF8C00;
}

/* 毛玻璃卡片 */
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #FF8C00 0%, #FF6B00 100%);
}

/* 品牌文化展示 */
.brand-culture-show {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #FFF5F0 0%, #FEF3C7 100%);
  border: 1px solid rgba(255, 140, 0, 0.3);
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
  color: #FF8C00;
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
  color: #FF8C00;
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
  box-shadow: 0 8px 24px rgba(255, 140, 0, 0.4);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.login-header h1 {
  font-size: 26px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.login-header p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 16px 0;
}

.tagline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 13px;
  color: #FF8C00;
  font-weight: 500;
}

.login-form {
  margin-top: 24px;
}

/* 自定义输入框 */
:deep(.custom-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  box-shadow: none;
  padding: 4px 12px;
}

:deep(.custom-input .el-input__wrapper:hover) {
  border-color: rgba(255, 140, 0, 0.5);
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: #ff8c00;
  box-shadow: 0 0 0 3px rgba(255, 140, 0, 0.15);
}

:deep(.custom-input .el-input__inner) {
  color: #ffffff;
}

:deep(.custom-input .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.custom-input .el-input__prefix .el-icon) {
  color: rgba(255, 255, 255, 0.5);
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff8c00 0%, #ffaa33 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 140, 0, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.brand-button {
  background: linear-gradient(135deg, #FF8C00 0%, #FF6B00 100%);
  border: none;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(255, 140, 0, 0.3);
  font-weight: 600;
}

.brand-button:hover {
  box-shadow: 0 6px 20px rgba(255, 140, 0, 0.4);
}

/* 底部链接 */
.login-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.login-footer .el-link {
  margin: 0 6px;
  font-weight: 500;
}

.login-footer .el-link:hover {
  color: #ffaa33;
}

.login-footer .separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.3);
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
  background: linear-gradient(135deg, #FF8C00 0%, #FF6B00 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #FFFFFF;
  box-shadow: 0 4px 12px rgba(255, 140, 0, 0.3);
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
  color: #FF6B00;
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
  color: #FF8C00;
}

.brand-footer-text p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-weight: 500;
}
</style>
