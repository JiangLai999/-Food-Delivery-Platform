<template>
  <div class="messages-container">
    <el-row :gutter="20">
      <!-- 左侧：消息列表 -->
      <el-col :span="8">
        <el-card class="message-list-card">
          <template #header>
            <div class="card-header">
              <span>消息列表</span>
            </div>
          </template>
          
          <el-tabs v-model="activeTab" class="message-tabs">
            <el-tab-pane label="系统公告" name="notices">
              <div v-if="noticeLoading" class="loading-container">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
              <div v-else-if="notices.length === 0" class="empty-container">
                <el-empty description="暂无公告" :image-size="60" />
              </div>
              <div v-else class="message-list">
                <div
                  v-for="notice in notices"
                  :key="notice.id"
                  class="message-item"
                  :class="{ active: activeTab === 'notices' && currentNotice?.id === notice.id }"
                  @click="selectNotice(notice)"
                >
                  <div class="message-icon notice-icon">
                    <el-icon><Bell /></el-icon>
                  </div>
                  <div class="message-content">
                    <div class="message-title">{{ notice.title }}</div>
                    <div class="message-time">{{ formatTime(notice.publishTime || notice.createTime) }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="用户咨询" name="chats">
              <div v-if="chatLoading" class="loading-container">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
              <div v-else-if="chatList.length === 0" class="empty-container">
                <el-empty description="暂无咨询" :image-size="60" />
              </div>
              <div v-else class="message-list">
                <div
                  v-for="chat in chatList"
                  :key="chat.userId"
                  class="message-item"
                  :class="{ active: activeTab === 'chats' && currentChat?.userId === chat.userId }"
                  @click="selectChat(chat)"
                >
                  <el-avatar :size="40" :src="chat.userAvatar">
                    {{ chat.userName?.charAt(0) || '用' }}
                  </el-avatar>
                  <div class="message-content">
                    <div class="message-title">{{ chat.userName || '用户咨询' }}</div>
                    <div class="message-preview">{{ chat.lastMessage }}</div>
                  </div>
                  <el-badge :value="chat.unreadCount" :hidden="!chat.unreadCount" />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
      
      <!-- 右侧：消息详情/聊天 -->
      <el-col :span="16">
        <el-card class="chat-card" v-if="activeTab === 'notices' && currentNotice">
          <template #header>
            <div class="card-header">
              <span>公告详情</span>
            </div>
          </template>
          <div class="notice-detail">
            <h2 class="notice-title">{{ currentNotice.title }}</h2>
            <div class="notice-time">{{ formatTime(currentNotice.publishTime || currentNotice.createTime) }}</div>
            <el-divider />
            <div class="notice-content">{{ currentNotice.content }}</div>
          </div>
        </el-card>
        
        <el-card class="chat-card" v-else-if="activeTab === 'chats' && currentChat">
          <template #header>
            <div class="card-header">
              <span>{{ currentChat.userName || '用户咨询' }}</span>
            </div>
          </template>
          
          <div class="chat-container" ref="chatContainer">
            <div
              v-for="(msg, index) in chatMessages"
              :key="index"
              class="chat-message"
              :class="{ 'is-me': msg.fromUserType === 2 }"
            >
              <el-avatar v-if="msg.fromUserType !== 2" :size="36" :src="currentChat.userAvatar">
                {{ currentChat.userName?.charAt(0) || '用' }}
              </el-avatar>
              <div class="chat-bubble">
                <div class="chat-content">{{ msg.content }}</div>
                <div class="chat-time">{{ formatTime(msg.createTime) }}</div>
              </div>
              <el-avatar v-if="msg.fromUserType === 2" :size="36">
                {{ merchantName?.charAt(0) || '商' }}
              </el-avatar>
            </div>
          </div>
          
          <div class="quick-replies" v-if="quickReplies.length > 0">
            <span class="quick-reply-label">快捷回复：</span>
            <el-tag
              v-for="(reply, index) in quickReplies"
              :key="index"
              class="quick-reply-tag"
              @click="selectQuickReply(reply)"
            >
              {{ reply }}
            </el-tag>
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              placeholder="请输入消息..."
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button @click="sendMessage" :disabled="!inputMessage.trim()">
                  <el-icon><Promotion /></el-icon>
                  发送
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>
        
        <el-card class="chat-card" v-else>
          <el-empty description="请选择要查看的消息">
            <template #image>
              <el-icon :size="80" color="#909399"><ChatDotRound /></el-icon>
            </template>
          </el-empty>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantNotices, getChatHistory, getChatConversations, sendChatMessage } from '../api/merchant'
import { useUserStore } from '../store'

const userStore = useUserStore()
const merchantId = computed(() => userStore.merchantInfo?.id)
const merchantName = computed(() => userStore.merchantInfo?.merchantName)

const activeTab = ref('notices')
const notices = ref([])
const chatList = ref([])
const currentNotice = ref(null)
const currentChat = ref(null)
const chatMessages = ref([])
const inputMessage = ref('')
const noticeLoading = ref(false)
const chatLoading = ref(false)
const ws = ref(null)
const isWsConnected = ref(false)
const chatContainer = ref(null)

onMounted(() => {
  // 清除可能存在的缓存聊天数据
  localStorage.removeItem('merchant_chat_list')
  localStorage.removeItem('merchant_chat_messages')
  
  loadNotices()
  loadChatList()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
  if (ws.value) {
    try {
      ws.value.onclose = null
      ws.value.onerror = null
      ws.value.onmessage = null
    } catch (e) {
      console.error('清理WebSocket监听器失败:', e)
    }
  }
})

const quickReplies = ref([
  '您好，请问有什么可以帮助您的？',
  '感谢您的反馈，我们会尽快处理。',
  '抱歉给您带来不便，我们会立即改进。',
  '您的订单已收到，我们会尽快安排配送。',
  '如有问题，请随时联系我们。',
  '祝您用餐愉快！',
  '感谢您的支持与理解。',
  '我们会尽快为您解决问题。'
])

// WebSocket连接
const connectWebSocket = () => {
  if (!merchantId.value) return
  
  // 获取当前页面的主机名和端口，使用merchant_前缀区分商家连接
  const wsUrl = `ws://${window.location.hostname}:8080/api/native-ws/merchant_${merchantId.value}`
  console.log('Connecting to WebSocket:', wsUrl)
  ws.value = new WebSocket(wsUrl)
  
  ws.value.onopen = () => {
    console.log('WebSocket connected')
    isWsConnected.value = true
  }
  
  ws.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'CHAT_MESSAGE') {
        const msgData = data.data
        console.log('商家WebSocket收到消息:', msgData)
        
        // 只处理发给商家的消息（toUserType === 2）
        if (msgData.toUserType !== 2) {
          console.log('忽略非商家消息，toUserType:', msgData.toUserType)
          return
        }
        
        // 只处理来自用户的消息（fromUserType === 1）
        if (msgData.fromUserType !== 1) {
          console.log('忽略非用户消息，fromUserType:', msgData.fromUserType)
          return
        }
        
        if (currentChat.value && msgData.fromUserId === currentChat.value.userId) {
          chatMessages.value.push({
            fromUserId: msgData.fromUserId,
            fromUserType: msgData.fromUserType || 1,
            content: msgData.content,
            createTime: msgData.timestamp
          })
          scrollToBottom()
        } else {
          // 更新未读数
          const chat = chatList.value.find(c => c.userId === msgData.fromUserId)
          if (chat) {
            chat.unreadCount = (chat.unreadCount || 0) + 1
            chat.lastMessage = msgData.content
          }
        }
      }
    } catch (e) {
      console.error('Parse message error:', e)
    }
  }
  
  ws.value.onclose = () => {
    console.log('WebSocket disconnected')
    isWsConnected.value = false
    // 尝试重连
    setTimeout(connectWebSocket, 3000)
  }
  
  ws.value.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
}

// 断开WebSocket
const disconnectWebSocket = () => {
  if (ws.value) {
    ws.value.close()
    ws.value = null
  }
}

const loadNotices = async () => {
  noticeLoading.value = true
  try {
    const data = await getMerchantNotices()
    notices.value = data || []
  } catch (error) {
    console.error('加载公告失败:', error)
  } finally {
    noticeLoading.value = false
  }
}

const loadChatList = async () => {
  chatLoading.value = true
  try {
    const data = await getChatConversations()
    console.log('商家加载聊天列表:', data)
    chatList.value = data || []
  } catch (error) {
    console.error('加载咨询列表失败:', error)
  } finally {
    chatLoading.value = false
  }
}

const loadChatMessages = async () => {
  if (!currentChat.value?.userId) return
  try {
    const data = await getChatHistory(currentChat.value.userId)
    // 去重：根据消息ID去重
    const uniqueMessages = []
    const seenIds = new Set()
    if (data && data.length > 0) {
      for (const msg of data) {
        const msgId = msg.id
        if (!seenIds.has(msgId)) {
          seenIds.add(msgId)
          uniqueMessages.push(msg)
        }
      }
    }
    chatMessages.value = uniqueMessages || []
    scrollToBottom()
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
}

const selectNotice = (notice) => {
  currentNotice.value = notice
}

const selectChat = (chat) => {
  currentChat.value = chat
  loadChatMessages()
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentChat.value?.userId) return
  
  try {
    await sendChatMessage({
      toUserId: currentChat.value.userId,
      toUserType: 1,
      content: inputMessage.value.trim(),
      contentType: 0
    })
    inputMessage.value = ''
    loadChatMessages()
    ElMessage.success('发送成功')
  } catch (error) {
    console.error('发送失败:', error)
    ElMessage.error('发送失败')
  }
}

const selectQuickReply = (reply) => {
  inputMessage.value = reply
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string') return time
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.messages-container {
  min-height: 100%;
}

.message-list-card {
  height: calc(100vh - 140px);
}

.message-tabs {
  height: calc(100% - 50px);
}

.message-tabs :deep(.el-tabs__content) {
  height: 100%;
  overflow-y: auto;
}

.message-list {
  display: flex;
  flex-direction: column;
}

.message-item {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.active {
  background-color: #fff7ed;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.notice-icon {
  background-color: #fff7ed;
  color: #ff8c00;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.message-preview {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading-container,
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.chat-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.notice-detail {
  padding: 0 20px;
}

.notice-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
}

.notice-time {
  font-size: 13px;
  color: #909399;
}

.notice-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.chat-message.is-me {
  flex-direction: row-reverse;
}

.chat-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.chat-message.is-me .chat-bubble {
  background-color: #fff7ed;
}

.chat-content {
  font-size: 14px;
  color: #303133;
  word-break: break-word;
}

.chat-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.quick-replies {
  padding: 8px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.quick-reply-label {
  font-size: 12px;
  color: #909399;
}

.quick-reply-tag {
  cursor: pointer;
}

.quick-reply-tag:hover {
  background-color: #fff7ed;
  border-color: #ff8c00;
  color: #ff8c00;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
