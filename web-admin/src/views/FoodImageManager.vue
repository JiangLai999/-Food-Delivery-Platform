<template>
  <div class="food-image-manager" v-if="visible">
    <div class="overlay" @click="close"></div>
    <div class="panel">
      <div class="panel-header" style="display:flex; justify-content: space-between; align-items:center; padding:8px 12px; border-bottom:1px solid #eee;">
        <strong>餐品图片管理</strong>
        <div>
          <button class="btn" @click="reloadFoods">刷新</button>
          <button class="btn secondary" @click="close" style="margin-left:8px">关闭</button>
        </div>
      </div>
      <div class="panel-body" style="max-height:60vh; overflow:auto; padding:12px;">
        <div v-for="food in foods" :key="food.id" class="food-card" style="border:1px solid #eee; padding:12px; border-radius:8px; margin-bottom:12px;">
          <div style="font-weight:600; margin-bottom:6px;">{{ food.foodName || food.name }}</div>
          <div class="image-grid" style="display:flex; flex-wrap:wrap; gap:8px; align-items:center; position: relative;">
            <div v-for="(src, idx) in getImages(food)" :key="idx" style="position: relative; display: inline-block;">
              <img :src="imageURL(src)" style="width:90px; height:90px; object-fit:cover; border-radius:6px; border:1px solid #ddd;" />
              <button @click="removeFoodImage(food.id, idx)" style="position:absolute; top:-6px; right:-6px; width:20px; height:20px; border-radius:50%; background:#f56; color:white; border:0; font-size:12px; line-height:20px; padding:0;">×</button>
            </div>
          </div>
          <div style="margin-top:8px;">上传新图片：
            <input type="file" @change="e => onFileChange(e, food.id)" accept="image/*" />
          </div>
        </div>
      </div>
    </div>
  </div>
  
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const visible = ref(true)
const emit = defineEmits(['close'])
const foods = ref([])

const imageBase = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  if (p.startsWith('/images/')) return `${baseUrl}${p}`
  if (p.startsWith('images/')) return `${baseUrl}/${p}`
  return `${baseUrl}/images/${p}`
}

const getImages = (food) => {
  const v = (food?.image) || ''
  if (!v) return []
  return v.split(',').filter(Boolean).map(img => img.trim())
}

const imageURL = (src) => imageBase(src)

const loadFoods = async () => {
  try {
    const res = await request.get('/food/list', { params: { page: 1, pageSize: 9999 } })
    const data = res?.data?.records ?? res?.data?.items ?? (Array.isArray(res?.data) ? res.data : [])
    foods.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('加载餐品失败', e)
  }
}

const onFileChange = async (event, foodId) => {
  const file = event.target.files?.[0]
  if (!file) return
  const form = new FormData()
  form.append('image', file)
  try {
    const resp = await request.post(`/food/upload-image/${foodId}`, form)
    const path = resp?.data ?? ''
    // 更新本地 foods 数据的 image 字段
    const idx = foods.value.findIndex(f => f.id === foodId)
    if (idx >= 0) {
      const f = foods.value[idx]
      const current = f.image || ''
      f.image = current ? current + ',' + path : path
      foods.value[idx] = { ...f }
    }
    ElMessage.success('图片上传成功')
  } catch (err) {
    ElMessage.error('图片上传失败: ' + (err?.message ?? '未知错误'))
  }
}

const reloadFoods = () => {
  loadFoods()
}

const close = () => {
  visible.value = false
  emit('close')
}

onMounted(loadFoods)

// 新增：删除单张图片并更新服务器端图片列表
const updateFoodImages = async (foodId, images) => {
  try {
    await request.put(`/food/update-image/${foodId}`, { images })
  } catch (e) {
    ElMessage.error('图片更新失败: ' + (e?.message ?? '未知错误'))
  }
}

const removeFoodImage = async (foodId, index) => {
  const food = foods.value.find(f => f.id === foodId)
  if (!food) return
  const imgs = getImages(food).slice()
  if (index >= 0 && index < imgs.length) {
    imgs.splice(index, 1)
    // 更新服务端
    await updateFoodImages(foodId, imgs)
    // 更新本地 UI 数据
    food.image = imgs.length > 0 ? imgs.join(',') : ''
    const i = foods.value.findIndex(f => f.id === foodId)
    if (i >= 0) foods.value[i] = { ...food }
  }
}
</script>

<style scoped>
.food-image-manager { position: fixed; inset: 0; z-index: 9999; }
.overlay { position: absolute; inset: 0; background: rgba(0,0,0,.5); }
.panel { position: absolute; top: 5%; left: 5%; right: 5%; bottom: 5%; background: #fff; border-radius: 8px; display:flex; flex-direction:column; }
.image-grid img { display:block; }
</style>
