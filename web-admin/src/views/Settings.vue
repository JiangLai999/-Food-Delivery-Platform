<template>
  <div class="settings-page">
    <el-card>
      <template #header>
        <span>系统设置</span>
      </template>

      <!-- 数据备份 -->
      <el-card class="setting-section" shadow="never">
        <template #header>
          <span>数据备份</span>
        </template>
        
        <div class="backup-actions">
          <el-button type="primary" :loading="backupLoading" @click="handleBackup">
            <el-icon><Download /></el-icon>
            立即备份
          </el-button>
          
          <el-alert
            v-if="backupInfo"
            :title="'备份成功：' + backupInfo.filename"
            type="success"
            :closable="false"
            class="backup-alert"
          >
            <template #default>
              <div>
                <p>路径：{{ backupInfo.path }}</p>
                <p>大小：{{ formatFileSize(backupInfo.size) }}</p>
                <p>时间：{{ backupInfo.timestamp }}</p>
                <el-button type="text" @click="downloadBackup(backupInfo.filename)">
                  下载备份文件
                </el-button>
              </div>
            </template>
          </el-alert>
        </div>
      </el-card>

      <!-- 系统信息 -->
      <el-card class="setting-section" shadow="never">
        <template #header>
          <span>系统信息</span>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
          <el-descriptions-item label="后端框架">Spring Boot 2.7.18</el-descriptions-item>
          <el-descriptions-item label="Java版本">JDK 11</el-descriptions-item>
          <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
          <el-descriptions-item label="管理端框架">Vue 3.3.4 + Element Plus 2.3.14 + Vite 4.4.9</el-descriptions-item>
          <el-descriptions-item label="移动端框架">Android (Kotlin) - compileSdk 35, minSdk 24, targetSdk 34</el-descriptions-item>
          <el-descriptions-item label="MyBatis-Plus">3.5.3.1</el-descriptions-item>
          <el-descriptions-item label="地图服务">高德地图 SDK</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 修改密码 -->
      <el-card class="setting-section" shadow="never">
        <template #header>
          <span>修改密码</span>
        </template>
        
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import { Download } from '@element-plus/icons-vue';
import { markRaw } from 'vue';
import request from '../utils/request';

export default {
  name: 'Settings',
  data() {
    return {
      Download: markRaw(Download),
      
      // 备份相关
      backupLoading: false,
      backupInfo: null,
      
      // 密码修改相关
      passwordLoading: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.passwordForm.newPassword) {
                callback(new Error('两次输入的密码不一致'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ]
      }
    };
  },
  methods: {
    // 执行数据备份
    async handleBackup() {
      this.backupLoading = true;
      try {
        const response = await request({
          url: '/admin/backup',
          method: 'post'
        });
        
        this.$message.success('数据备份成功');
        const timestamp = new Date().toLocaleString();
        this.backupInfo = { 
          filename: response.data || 'backup.sql', 
          path: 'backups/' + (response.data || 'backup.sql'),
          size: 0,
          timestamp: timestamp
        };
      } catch (error) {
        console.error('备份失败:', error);
        let errorMsg = '备份失败';
        if (error.message && error.message.includes('mysqldump')) {
          errorMsg = 'mysqldump命令不可用，请确保已安装MySQL并配置了环境变量';
        } else if (error.message) {
          errorMsg = error.message;
        }
        this.$message.error(errorMsg);
      } finally {
        this.backupLoading = false;
      }
    },
    
    // 下载备份文件
    downloadBackup(filename) {
      window.open(`/api/admin/backup/download?filename=${encodeURIComponent(filename)}`, '_blank');
    },
    
    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    // 修改密码
    async handleChangePassword() {
      const valid = await this.$refs.passwordFormRef.validate();
      if (!valid) return;
      
      this.passwordLoading = true;
      try {
        await request({
          url: '/admin/password',
          method: 'put',
          params: {
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          }
        });
        
        this.$message.success('密码修改成功，请重新登录');
        localStorage.removeItem('token');
        this.$router.push('/login');
      } catch (error) {
        this.$message.error('密码修改失败');
      } finally {
        this.passwordLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.settings-page {
  padding: 20px;
}

.setting-section {
  margin-bottom: 20px;
}

:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #ff8c00 0%, #ffa033 100%);
  border: none;
}

.backup-actions {
  padding: 20px 0;
}

.backup-alert {
  margin-top: 20px;
}
</style>
