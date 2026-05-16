<template>
  <div class="account-manage">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <span>账户信息</span>
          </template>
          <div class="account-info">
            <el-avatar :size="80" :src="accountInfo.avatar" class="avatar">
              {{ accountInfo.merchantName?.charAt(0) || '商' }}
            </el-avatar>
            <div class="info-item">
              <span class="label">商家名称：</span>
              <span class="value">{{ accountInfo.merchantName }}</span>
            </div>
            <div class="info-item">
              <span class="label">登录手机：</span>
              <span class="value">{{ accountInfo.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">账户状态：</span>
              <el-tag :type="accountInfo.status === 1 ? 'success' : 'warning'">
                {{ accountInfo.status === 1 ? '正常' : '待审核' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" label-width="100px" style="max-width: 400px;">
                <el-form-item label="当前密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="changing">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="绑定手机" name="phone">
              <el-form :model="phoneForm" label-width="100px" style="max-width: 400px;">
                <el-form-item label="新手机号">
                  <el-input v-model="phoneForm.phone" placeholder="请输入新手机号" />
                </el-form-item>
                <el-form-item label="验证码">
                  <el-input v-model="phoneForm.code" placeholder="请输入验证码" style="width: 180px;" />
                  <el-button 
                    @click="handleSendCode" 
                    :disabled="countdown > 0"
                    style="margin-left: 10px;"
                  >
                    {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
                  </el-button>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePhone" :loading="changing">
                    更换手机号
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store'
import request from '../utils/request'

const userStore = useUserStore()

const activeTab = ref('password')
const changing = ref(false)
const countdown = ref(0)

const accountInfo = computed(() => userStore.merchantInfo || {})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const phoneForm = reactive({
  phone: '',
  code: ''
})

let countdownTimer = null

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  
  changing.value = true
  try {
    await request({
      url: '/merchant/password',
      method: 'post',
      data: {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      }
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    setTimeout(() => {
      userStore.clearAuth()
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    changing.value = false
  }
}

const handleSendCode = async () => {
  if (!phoneForm.phone || !/^1\d{10}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    await request({
      url: '/merchant/sms/send',
      method: 'post',
      data: { phone: phoneForm.phone }
    })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

const handleChangePhone = async () => {
  if (!phoneForm.phone || !/^1\d{10}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!phoneForm.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  changing.value = true
  try {
    await request({
      url: '/merchant/phone',
      method: 'post',
      data: {
        phone: phoneForm.phone,
        code: phoneForm.code
      }
    })
    ElMessage.success('手机号更换成功')
    phoneForm.phone = ''
    phoneForm.code = ''
  } catch (error) {
    console.error('更换手机号失败:', error)
  } finally {
    changing.value = false
  }
}

onMounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.account-manage {
  min-height: 100%;
}

.info-card {
  height: 100%;
}

.account-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.avatar {
  font-size: 32px;
}

.info-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-item .label {
  color: #909399;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}
</style>
