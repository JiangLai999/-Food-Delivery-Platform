import request from '../utils/request'

export function getAdminConversations() {
  console.log('=== 调用getAdminConversations API ===')
  const promise = request({
    url: '/chat/admin/conversations',
    method: 'get'
  })
  promise.then(data => {
    console.log('getAdminConversations返回数据:', data)
    console.log('返回数据类型:', typeof data)
    console.log('返回数据长度:', data?.length || 0)
  }).catch(err => {
    console.error('getAdminConervations错误:', err)
  })
  return promise
}

export function getAdminChatHistory(userId, userType) {
  console.log('=== 调用getAdminChatHistory API ===', { userId, userType })
  const promise = request({
    url: '/chat/admin/history',
    method: 'get',
    params: { userId, userType, _t: Date.now() }
  })
  promise.then(data => {
    console.log('getAdminChatHistory返回数据:', data?.length || 0, '条')
  })
  return promise
}

export function sendChatMessage(data) {
  console.log('=== 发送消息 ===', data)
  return request({
    url: '/chat/send',
    method: 'post',
    data
  })
}

export function deleteConversation(userId, userType) {
  return request({
    url: '/chat/admin/conversation',
    method: 'delete',
    params: { userId, userType, _t: Date.now() }
  })
}
