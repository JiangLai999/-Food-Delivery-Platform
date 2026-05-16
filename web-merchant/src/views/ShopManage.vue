<template>
  <div class="shop-manage">
    <el-card class="shop-card">
      <template #header>
        <div class="card-header">
          <span>店铺信息</span>
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存修改
          </el-button>
        </div>
      </template>
      
      <el-form :model="shopForm" label-width="100px" class="shop-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="店铺名称">
              <el-input v-model="shopForm.merchantName" placeholder="请输入店铺名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="店铺Logo">
              <el-upload
                class="avatar-uploader"
                action="/api/image/upload"
                :show-file-list="false"
                :on-success="handleLogoSuccess"
                :before-upload="beforeUpload"
                :data="{ type: 'merchant-avatar' }"
              >
                <img v-if="shopForm.logo" :src="getImageUrl(shopForm.logo)" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="shopForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="shopForm.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="店铺分类">
          <el-select v-model="shopForm.categoryId" placeholder="请选择店铺分类" style="width: 100%;">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="所在地区">
          <el-cascader
            v-model="shopForm.address"
            :options="regionOptions"
            placeholder="请选择省市区"
            style="width: 100%;"
            @change="handleAddressChange"
          />
        </el-form-item>
        
        <el-form-item label="详细地址">
          <el-input v-model="shopForm.detailAddress" placeholder="请输入详细地址" />
        </el-form-item>
        
        <el-form-item label="营业时间">
          <el-time-picker
            v-model="shopForm.businessHoursStart"
            placeholder="开始时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 150px;"
          />
          <span style="margin: 0 10px;">至</span>
          <el-time-picker
            v-model="shopForm.businessHoursEnd"
            placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 150px;"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="起送价">
              <el-input-number v-model="shopForm.minOrderAmount" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="配送费">
              <el-input-number v-model="shopForm.deliveryFee" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="店铺状态">
              <el-select v-model="shopForm.status" style="width: 100%;">
                <el-option :value="1" label="正常营业" />
                <el-option :value="2" label="休息中" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="店铺简介">
          <el-input
            v-model="shopForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入店铺简介"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { getShopCategories } from '../api/merchant'

const saving = ref(false)
const categories = ref([])

const shopForm = reactive({
  merchantName: '',
  logo: '',
  contactPhone: '',
  contactPerson: '',
  categoryId: null,
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  address: [],
  businessHoursStart: '',
  businessHoursEnd: '',
  minOrderAmount: 0,
  deliveryFee: 0,
  status: 1,
  description: ''
})

const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [{ value: '北京市', label: '北京市' }]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [{ value: '上海市', label: '上海市' }]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      { value: '广州市', label: '广州市' },
      { value: '深圳市', label: '深圳市' },
      { value: '佛山市', label: '佛山市' }
    ]
  }
]

const loadShopInfo = async () => {
  try {
    const data = await request({
      url: '/merchant/shop',
      method: 'get'
    })
    Object.assign(shopForm, data)
    if (data.province) {
      shopForm.address = [data.province, data.city]
    }
    if (data.businessHours) {
      const hours = data.businessHours.split('-')
      shopForm.businessHoursStart = hours[0]
      shopForm.businessHoursEnd = hours[1]
    }
  } catch (error) {
    console.error('加载店铺信息失败:', error)
  }
}

const loadCategories = async () => {
  try {
    const data = await getShopCategories()
    categories.value = data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleAddressChange = (value) => {
  if (value && value.length >= 2) {
    shopForm.province = value[0]
    shopForm.city = value[1]
  }
}

const handleLogoSuccess = (response) => {
  if (response.code === 200 && response.data) {
    shopForm.logo = response.data
    ElMessage.success('Logo上传成功')
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

const handleSave = async () => {
  saving.value = true
  try {
    const submitData = {
      ...shopForm,
      businessHours: shopForm.businessHoursStart && shopForm.businessHoursEnd
        ? `${shopForm.businessHoursStart}-${shopForm.businessHoursEnd}`
        : ''
    }
    delete submitData.address
    
    await request({
      url: '/merchant/shop',
      method: 'put',
      data: submitData
    })
    
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadShopInfo()
  loadCategories()
})
</script>

<style scoped>
.shop-manage {
  min-height: 100%;
}

.shop-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shop-form {
  padding: 20px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #ff8c00;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style>
