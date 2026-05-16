<template>
  <div class="delivery-board">
    <div class="banner" style="font-weight:600; padding:8px 12px; border-bottom:1px solid #eee;">配送看板</div>
    <div class="delivery-list" style="padding:12px; display:flex; flex-direction:column; gap:8px;">
      <div v-for="t in tasks" :key="t.orderId" class="delivery-item" style="border:1px solid #eee; padding:10px; border-radius:6px; background:#fff;">
        <div><strong>订单ID:</strong> {{ t.orderId }}</div>
        <div><strong>骑手:</strong> {{ t.riderName || '未分配' }}</div>
        <div><strong>状态:</strong> {{ statusText(t.status) }}</div>
        <div><strong>预计送达:</strong> {{ t.estimatedTime ?? '-' }} 分钟</div>
      </div>
    </div>
  </div>
  
</template>

<script setup>
import { ref, onMounted } from 'vue'
const tasks = ref([])

async function loadTasks(){
  try {
    const resp = await fetch('/admin/delivery/tasks')
    if (!resp.ok) return
    const data = await resp.json()
    // 兼容 API 返回结构：{ code, data } 或直接 data 列表
    const payload = data?.data ?? data
    if (Array.isArray(payload)) {
      tasks.value = payload
    }
  } catch (e) {
    console.error('DeliveryBoard load failed', e)
  }
}

onMounted(loadTasks)

function statusText(s){
  switch(s){
    case 0: return '待取餐'
    case 1: return '配送中'
    case 2: return '已送达'
    default: return '未知'
  }
}
</script>

<style scoped>
.delivery-board { background: #fff; border-radius: 8px; box-shadow: var(--shadow, 0 2px 8px rgba(0,0,0,.05)); padding:0; }
.delivery-board .banner { background: #fff; }
.delivery-item { padding:8px; }
</style>
