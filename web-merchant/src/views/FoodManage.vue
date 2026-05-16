<template>
  <div class="food-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜品列表</span>
          <div>
            <el-button @click="showCategoryDialog">
              分类管理
            </el-button>
            <el-button type="primary" @click="router.push('/foods/add')">
              <el-icon><Plus /></el-icon>
              添加菜品
            </el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="菜品名称">
          <el-input v-model="queryForm.foodName" placeholder="请输入菜品名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="全部" :value="null" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" clearable style="width: 150px;">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadFoods">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="foods" v-loading="loading" style="width: 100%;">
        <el-table-column prop="image" label="菜品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.image)"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 8px;"
              :preview-src-list="[getImageUrl(row.image)]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="foodName" label="菜品名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #ff8c00; font-weight: 600;">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <span style="text-decoration: line-through; color: #909399;">
              ¥{{ row.originalPrice }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="salesVolume" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="loadFoods"
        @current-change="loadFoods"
      />
    </el-card>

    <el-dialog v-model="categoryDialogVisible" title="分类管理" width="500px">
      <div class="category-list">
        <div v-for="item in categories" :key="item.id" class="category-item">
          <span>{{ item.categoryName }}</span>
          <el-button type="danger" link @click="handleDeleteCategory(item)">删除</el-button>
        </div>
        <div v-if="categories.length === 0" class="empty-tip">暂无分类，请添加</div>
      </div>
      <el-divider />
      <el-form :inline="true" @submit.prevent="handleAddCategory">
        <el-form-item label="新分类">
          <el-input v-model="newCategoryName" placeholder="请输入分类名称" style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAddCategory">添加</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()

const loading = ref(false)
const foods = ref([])
const categories = ref([])
const categoryDialogVisible = ref(false)
const newCategoryName = ref('')

const queryForm = reactive({
  foodName: '',
  status: null,
  categoryId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const defaultFoodImage = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2YzZjRmNiIvPjx0ZXh0IHg9IjUwIiB5PSI1MCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iI2EwYTBhMCI+5rOV5ZOBPC90ZXh0Pjwvc3ZnPg=='

const getImageUrl = (path) => {
  if (!path) return defaultFoodImage
  if (path.startsWith('http')) return path
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${path}`
}

const loadFoods = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/merchant/foods',
      method: 'get',
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    // 后端返回 Page<FoodItem>，数据在 records 字段中
    const pageData = res || {}
    foods.value = pageData.records || []
    pagination.total = pageData.total || 0
  } catch (error) {
    console.error('加载菜品列表失败:', error)
  } finally {
    loading.value = false
  }
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

const handleReset = () => {
  queryForm.foodName = ''
  queryForm.status = null
  queryForm.categoryId = null
  pagination.page = 1
  loadFoods()
}

const handleEdit = (id) => {
  router.push(`/foods/edit/${id}`)
}

const handleToggleStatus = async (row) => {
  try {
    await request({
      url: `/merchant/foods/${row.id}/status`,
      method: 'put',
      data: { status: row.status === 1 ? 0 : 1 }
    })
    ElMessage.success(row.status === 1 ? '已下架' : '已上架')
    loadFoods()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该菜品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/merchant/foods/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      loadFoods()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

const showCategoryDialog = () => {
  categoryDialogVisible.value = true
  newCategoryName.value = ''
}

const handleAddCategory = async () => {
  if (!newCategoryName.value.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    await request({
      url: '/merchant/food-categories',
      method: 'post',
      data: {
        categoryName: newCategoryName.value.trim(),
        sortOrder: 0
      }
    })
    ElMessage.success('添加成功')
    newCategoryName.value = ''
    loadCategories()
  } catch (error) {
    console.error('添加分类失败:', error)
  }
}

const handleDeleteCategory = (item) => {
  ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/merchant/food-categories/${item.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      loadCategories()
    } catch (error) {
      console.error('删除分类失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadFoods()
  loadCategories()
})
</script>

<style scoped>
.food-manage {
  min-height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.category-list {
  max-height: 300px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.category-item:last-child {
  border-bottom: none;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}
</style>
