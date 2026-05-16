<template>
  <div class="messages-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="message-list-card">
          <template #header>
            <div class="card-header">
              <span>消息列表</span>
            </div>
          </template>
          
          <div v-if="loading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div v-else-if="conversationList.length === 0" class="empty-container">
            <el-empty description="暂无消息" :image-size="60" />
          </div>
          <div v-else class="message-list">
            <div
              v-for="chat in conversationList"
              :key="chat.userId + '_' + chat.userType"
              class="message-item"
              :class="{ active: currentChat?.userId === chat.userId && currentChat?.userType === chat.userType }"
              @click="selectChat(chat)"
            >
              <el-avatar :size="40" :src="chat.userAvatar">
                {{ chat.userName?.charAt(0) || '用' }}
              </el-avatar>
              <div class="message-content">
                <div class="message-title">{{ chat.userName || getUserTypeName(chat.userType) }}</div>
                <div class="message-preview">{{ chat.lastMessage }}</div>
              </div>
              <el-badge :value="chat.unreadCount" :hidden="!chat.unreadCount" />
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="chat-card" v-if="currentChat">
          <template #header>
            <div class="card-header">
              <span>{{ currentChat.userName || getUserTypeName(currentChat.userType) }}</span>
              <el-button type="danger" size="small" text @click="handleDeleteChat">
                <el-icon><Delete /></el-icon>
                删除聊天
              </el-button>
            </div>
          </template>
          
          <div class="chat-container" ref="chatContainer">
            <div
              v-for="(msg, index) in chatMessages"
              :key="index"
              class="chat-message"
              :class="{ 'is-me': msg.fromUserType === 3 }"
            >
              <el-avatar v-if="msg.fromUserType !== 3" :size="36" :src="currentChat.userAvatar">
                {{ currentChat.userName?.charAt(0) || '用' }}
              </el-avatar>
              <div class="chat-bubble">
                <div class="chat-content">{{ msg.content }}</div>
                <div class="chat-time">{{ formatTime(msg.createTime) }}</div>
              </div>
              <el-avatar v-if="msg.fromUserType === 3" :size="36" style="background: linear-gradient(135deg, #ff8c00, #ffa033);">
                管
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
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminConversations, getAdminChatHistory, sendChatMessage, deleteConversation } from '../api/chat'
import { Loading, Promotion, ChatDotRound, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const conversationList = ref([])
const currentChat = ref(null)
const chatMessages = ref([])
const inputMessage = ref('')
const chatContainer = ref(null)
const ws = ref(null)
const adminId = ref(null)

onMounted(() => {
  // 清除可能存在的缓存聊天数据
  localStorage.removeItem('admin_chat_list')
  localStorage.removeItem('admin_chat_messages')
  
  loadConversations()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
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

const getToken = () => {
  return localStorage.getItem('token')
}

const getAdminIdFromToken = () => {
  try {
    const token = getToken()
    if (!token) return null
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.userId
  } catch (e) {
    return null
  }
}

const connectWebSocket = () => {
  adminId.value = getAdminIdFromToken()
  if (!adminId.value) return
  
  const wsUrl = `ws://${window.location.hostname}:8080/api/native-ws/admin_${adminId.value}`
  console.log('连接WebSocket:', wsUrl)
  
  ws.value = new WebSocket(wsUrl)
  
  ws.value.onopen = () => {
    console.log('WebSocket connected')
    ElMessage.success('已连接消息服务')
  }
  
  ws.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'CHAT_MESSAGE') {
        const msgData = data.data
        if (currentChat.value && 
            ((msgData.fromUserId === currentChat.value.userId && msgData.fromUserType === currentChat.value.userType) ||
             (msgData.toUserId === adminId.value && msgData.toUserType === 3))) {
          chatMessages.value.push({
            fromUserId: msgData.fromUserId,
            fromUserType: msgData.fromUserType,
            content: msgData.content,
            createTime: msgData.timestamp
          })
          scrollToBottom()
        } else {
          const chat = conversationList.value.find(c => 
            c.userId === msgData.fromUserId && c.userType === msgData.fromUserType
          )
          if (chat) {
            chat.unreadCount = (chat.unreadCount || 0) + 1
            chat.lastMessage = msgData.content
          }
          ElMessage.info('收到新消息')
        }
      }
    } catch (e) {
      console.error('解析消息失败:', e)
    }
  }
  
  ws.value.onclose = () => {
    console.log('WebSocket disconnected')
    setTimeout(connectWebSocket, 3000)
  }
  
  ws.value.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
}

const getUserTypeName = (type) => {
  if (type === 1) return '用户'
  if (type === 2) return '商家'
  return '用户'
}

const loadConversations = async () => {
  loading.value = true
  try {
    console.log('开始加载管理员会话列表...')
    const data = await getAdminConversations()
    console.log('管理员会话列表响应:', data)
    
    conversationList.value = data || []
    console.log('设置会话列表后，数组长度:', conversationList.value.length)
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    loading.value = false
  }
}

const loadChatMessages = async () => {
  if (!currentChat.value?.userId || !currentChat.value?.userType) return
  try {
    const data = await getAdminChatHistory(currentChat.value.userId, currentChat.value.userType)
    chatMessages.value = data || []
    scrollToBottom()
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
}

const selectChat = (chat) => {
  console.log('选择聊天: ', chat)
  currentChat.value = chat
  loadChatMessages()
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentChat.value) return
  
  const toUserId = currentChat.value.userId
  const toUserType = currentChat.value.userType
  
  console.log('发送消息:', { toUserId, toUserType, content: inputMessage.value.trim() })
  
  if (!toUserId || !toUserType) {
    ElMessage.error('聊天信息不完整，无法发送')
    return
  }
  
  try {
    await sendChatMessage({
      toUserId: toUserId,
      toUserType: toUserType,
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

const handleDeleteChat = async () => {
  if (!currentChat.value) return
  
  const userId = currentChat.value.userId
  const userType = currentChat.value.userType
  
  console.log('删除聊天: userId=', userId, 'userType=', userType)
  
  if (!userId || !userType) {
    ElMessage.error('聊天信息不完整')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除与该用户的聊天记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteConversation(userId, userType)
    currentChat.value = null
    chatMessages.value = []
    loadConversations()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
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

onMounted(() => {
  // 清除可能存在的缓存聊天数据
  localStorage.removeItem('admin_chat_list')
  localStorage.removeItem('admin_chat_messages')
  
  loadConversations()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})
</script>

<style scoped>
.messages-container {
  min-height: 100%;
}

.message-list-card {
  height: calc(100vh - 140px);
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
