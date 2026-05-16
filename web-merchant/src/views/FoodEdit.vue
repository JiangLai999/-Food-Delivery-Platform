<template>
  <div class="food-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑菜品' : '添加菜品' }}</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <el-form :model="foodForm" label-width="100px" class="food-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜品名称" required>
              <el-input v-model="foodForm.foodName" placeholder="请输入菜品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜品分类" required>
              <el-select v-model="foodForm.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.categoryName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" required>
              <el-input-number v-model="foodForm.price" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="foodForm.originalPrice" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存">
              <el-input-number v-model="foodForm.stock" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="foodForm.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="菜品图片">
          <el-upload
            class="food-image-uploader"
            action="/api/image/upload"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeUpload"
            :data="uploadData"
          >
            <img v-if="foodForm.image" :src="getImageUrl(foodForm.image)" class="food-image" />
            <div v-else class="upload-placeholder">
              <el-icon :size="32"><Plus /></el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="菜品描述">
          <el-input
            v-model="foodForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入菜品描述"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="saving">
            {{ isEdit ? '保存修改' : '添加菜品' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()

const saving = ref(false)
const categories = ref([])
const foodId = computed(() => route.params.id)
const isEdit = computed(() => !!foodId.value)

const foodForm = reactive({
  foodName: '',
  categoryId: null,
  price: 0,
  originalPrice: null,
  stock: 999,
  status: 1,
  image: '',
  description: ''
})

const uploadData = computed(() => {
  const data = { type: 'food' }
  if (foodId.value) {
    data.foodId = foodId.value
  }
  return data
})

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${path}`
}

const loadCategories = async () => {
  try {
    const data = await request({
      url: '/merchant/food-categories',
      method: 'get'
    })
    categories.value = data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadFoodDetail = async () => {
  if (!foodId.value) return
  
  try {
    const data = await request({
      url: `/merchant/foods/${foodId.value}`,
      method: 'get'
    })
    Object.assign(foodForm, data)
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    ElMessage.error('加载失败')
    router.push('/foods')
  }
}

const handleImageSuccess = (response) => {
  // 后端返回 { code: 200, data: "/uploads/food/xxx.jpg" }
  if (response.code === 200 && response.data) {
    foodForm.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
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

const handleSubmit = async () => {
  if (!foodForm.foodName) {
    ElMessage.warning('请输入菜品名称')
    return
  }
  if (!foodForm.categoryId) {
    ElMessage.warning('请选择菜品分类')
    return
  }
  if (foodForm.price <= 0) {
    ElMessage.warning('请输入有效价格')
    return
  }
  
  saving.value = true
  try {
    if (isEdit.value) {
      await request({
        url: `/merchant/foods/${foodId.value}`,
        method: 'put',
        data: foodForm
      })
      ElMessage.success('保存成功')
    } else {
      await request({
        url: '/merchant/foods',
        method: 'post',
        data: foodForm
      })
      ElMessage.success('添加成功')
    }
    router.push('/foods')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadFoodDetail()
  }
})
</script>

<style scoped>
.food-edit {
  min-height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.food-form {
  padding: 20px;
  max-width: 800px;
}

.food-image-uploader {
  display: inline-block;
}

.food-image-uploader .el-upload {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
}

.food-image-uploader .el-upload:hover {
  border-color: #ff8c00;
}

.food-image {
  width: 200px;
  height: 150px;
  display: block;
  object-fit: cover;
}

.upload-placeholder {
  width: 200px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  background: #fafafa;
}

.upload-placeholder span {
  margin-top: 8px;
  font-size: 14px;
}
</style>
