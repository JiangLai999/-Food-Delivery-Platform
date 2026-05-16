<template>
  <div class="register-container">
    <div class="register-backdrop"></div>
    
    <!-- 右上角品牌标语 -->
    <div class="brand-tagline">
      <img src="/src/assets/logo.png" alt="秒送餐" class="header-logo-icon" />
      <div class="text">秒送餐 · 商家入驻</div>
    </div>
    
    <div class="register-box">
      <div class="register-header">
        <img src="/src/assets/logo.png" alt="秒送餐" class="brand-logo-img" />
        <h1>商家入驻</h1>
        <p>填写以下信息申请入驻平台</p>
      </div>
      
      <el-steps :active="step" finish-status="success" align-center class="register-steps">
        <el-step title="基本信息" />
        <el-step title="店铺信息" />
        <el-step title="资质认证" />
      </el-steps>
      
      <el-form ref="formRef" :model="registerForm" :rules="rules" class="register-form" label-position="top">
        <!-- 步骤1：基本信息 -->
        <div v-show="step === 0">
          <el-form-item label="手机号" prop="phone">
            <el-input 
              v-model="registerForm.phone" 
              placeholder="请输入手机号"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item label="登录密码" prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              show-password
              class="custom-input"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              class="custom-input"
            />
          </el-form-item>
        </div>
        
        <!-- 步骤2：店铺信息 -->
        <div v-show="step === 1">
          <el-form-item label="店铺名称" prop="merchantName">
            <el-input 
              v-model="registerForm.merchantName" 
              placeholder="请输入店铺名称"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item label="联系人" prop="contactPerson">
            <el-input 
              v-model="registerForm.contactPerson" 
              placeholder="请输入联系人姓名"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input 
              v-model="registerForm.contactPhone" 
              placeholder="请输入联系电话"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item label="详细地址" prop="detailAddress">
            <el-input 
              v-model="registerForm.detailAddress" 
              placeholder="请输入店铺详细地址"
              size="large"
              class="custom-input"
            />
          </el-form-item>
        </div>
        
        <!-- 步骤3：资质认证 -->
        <div v-show="step === 2">
          <el-form-item label="营业执照号" prop="licenseNumber">
            <el-input 
              v-model="registerForm.licenseNumber" 
              placeholder="请输入营业执照号"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item label="营业执照图片">
            <el-upload
              class="license-uploader"
              action="/api/image/upload"
              :show-file-list="false"
              :on-success="handleLicenseSuccess"
              :before-upload="beforeUpload"
              :data="{ type: 'merchant-license' }"
            >
              <img v-if="registerForm.licenseImage" :src="getImageUrl(registerForm.licenseImage)" class="license-image" />
              <div v-else class="license-placeholder">
                <el-icon :size="40"><Plus /></el-icon>
                <span>上传营业执照</span>
              </div>
            </el-upload>
          </el-form-item>
        </div>
        
        <div class="button-group">
          <el-button v-if="step > 0" size="large" @click="step--">上一步</el-button>
          <el-button v-if="step < 2" type="primary" size="large" @click="nextStep">下一步</el-button>
          <el-button v-if="step === 2" type="primary" size="large" :loading="loading" @click="handleRegister">
            提交申请
          </el-button>
        </div>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" :underline="false" @click="router.push('/login')">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { merchantRegister } from '../api/merchant'

const router = useRouter()

const formRef = ref()
const step = ref(0)
const loading = ref(false)

const registerForm = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  merchantName: '',
  contactPerson: '',
  contactPhone: '',
  detailAddress: '',
  licenseNumber: '',
  licenseImage: ''
})

const validatePassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  merchantName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' }
  ],
  licenseNumber: [
    { required: true, message: '请输入营业执照号', trigger: 'blur' }
  ]
}

const nextStep = async () => {
  if (step.value === 0) {
    try {
      await formRef.value.validateField(['phone', 'password', 'confirmPassword'])
      step.value = 1
    } catch (e) {
      return
    }
  } else if (step.value === 1) {
    try {
      await formRef.value.validateField(['merchantName'])
      step.value = 2
    } catch (e) {
      return
    }
  }
}

const handleLicenseSuccess = (response) => {
  if (response.code === 200 && response.data) {
    registerForm.licenseImage = response.data
    ElMessage.success('营业执照上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${path}`
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  
  loading.value = true
  try {
    await merchantRegister(registerForm)
    
    ElMessage.success('提交成功，请等待平台审核')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px 0;
}

.register-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='100%25' height='100%25' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3Cpattern id='grid' width='50' height='50' patternUnits='userSpaceOnUse'%3E%3Cpath d='M 50 0 L 0 0 0 50' fill='none' stroke='rgba(255, 140, 0, 0.03)' stroke-width='1'/%3E/pattern%3E%3C/defs%3E%3Crect width='100%25' height='100%25' fill='url(%23grid)'/%3E/svg%3E");
  opacity: 0.5;
  z-index: 0;
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

.register-box {
  position: relative;
  z-index: 1;
  width: 480px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-height: 90vh;
  overflow-y: auto;
}

.register-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #FF8C00 0%, #FFAA33 50%, #FF6B00 100%);
  border-radius: 20px 20px 0 0;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.brand-logo-img {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(255, 140, 0, 0.4);
}

.register-header h1 {
  font-size: 28px;
  color: #1A202C;
  margin: 0 0 8px 0;
  font-weight: 700;
}

.register-header p {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.register-steps {
  margin-bottom: 32px;
}

.register-steps :deep(.el-step__title) {
  font-size: 14px !important;
  font-weight: 600;
}

.register-form {
  margin-top: 10px;
}

.custom-input :deep(.el-input__wrapper) {
  background: rgba(255, 140, 0, 0.05);
  border: 2px solid rgba(255, 140, 0, 0.15);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 140, 0, 0.4);
  background: rgba(255, 140, 0, 0.08);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #FF8C00;
  background: rgba(255, 140, 0, 0.08);
  box-shadow: 0 0 0 4px rgba(255, 140, 0, 0.15);
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.button-group .el-button {
  flex: 1;
  border-radius: 12px;
  height: 44px;
  font-weight: 600;
}

.button-group .el-button--primary {
  background: linear-gradient(135deg, #FF8C00 0%, #FFAA33 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(255, 140, 0, 0.3);
}

.button-group .el-button--primary:hover {
  box-shadow: 0 6px 20px rgba(255, 140, 0, 0.4);
  transform: translateY(-2px);
}

.button-group .el-button--default {
  border: 2px solid rgba(255, 140, 0, 0.3);
  color: #FF8C00;
}

.button-group .el-button--default:hover {
  border-color: #FF8C00;
  background: rgba(255, 140, 0, 0.08);
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #718096;
}

.register-footer .el-link {
  margin-left: 6px;
  font-weight: 600;
  color: #FF8C00;
}

.register-footer .el-link:hover {
  color: #FF6B00;
}

.license-uploader {
  width: 100%;
}

.license-uploader :deep(.el-upload) {
  width: 100%;
}

.license-image {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  border-radius: 12px;
  border: 2px solid rgba(255, 140, 0, 0.2);
}

.license-placeholder {
  width: 100%;
  height: 180px;
  border: 2px dashed rgba(255, 140, 0, 0.3);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #718096;
  cursor: pointer;
  background: rgba(255, 140, 0, 0.02);
  transition: all 0.3s ease;
}

.license-placeholder:hover {
  border-color: #FF8C00;
  color: #FF8C00;
  background: rgba(255, 140, 0, 0.05);
}

.license-placeholder span {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 500;
}
</style>
