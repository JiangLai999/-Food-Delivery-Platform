import request from '../utils/request'

export const merchantLogin = (data) => {
  return request({
    url: '/merchant/login',
    method: 'post',
    data
  })
}

export const merchantRegister = (data) => {
  return request({
    url: '/merchant/register',
    method: 'post',
    data
  })
}

export const getShopCategories = () => {
  return request({
    url: '/merchant/shop-categories',
    method: 'get'
  })
}

export const getStatistics = () => {
  return request({
    url: '/merchant/statistics',
    method: 'get'
  })
}

export const getShopInfo = () => {
  return request({
    url: '/merchant/shop',
    method: 'get'
  })
}

export const updateShopInfo = (data) => {
  return request({
    url: '/merchant/shop',
    method: 'put',
    data
  })
}

export const getMerchantCategories = () => {
  return request({
    url: '/merchant/shop-categories',
    method: 'get'
  })
}

export const getFoodCategories = () => {
  return request({
    url: '/merchant/food-categories',
    method: 'get'
  })
}

export const getFoods = (params) => {
  return request({
    url: '/merchant/foods',
    method: 'get',
    params
  })
}

export const getFoodDetail = (id) => {
  return request({
    url: `/merchant/foods/${id}`,
    method: 'get'
  })
}

export const addFood = (data) => {
  return request({
    url: '/merchant/foods',
    method: 'post',
    data
  })
}

export const updateFood = (id, data) => {
  return request({
    url: `/merchant/foods/${id}`,
    method: 'put',
    data
  })
}

export const deleteFood = (id) => {
  return request({
    url: `/merchant/foods/${id}`,
    method: 'delete'
  })
}

export const toggleFoodStatus = (id, status) => {
  return request({
    url: `/merchant/foods/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export const getOrders = (params) => {
  return request({
    url: '/merchant/orders',
    method: 'get',
    params
  })
}

export const getOrderDetail = (id) => {
  return request({
    url: `/merchant/orders/${id}`,
    method: 'get'
  })
}

export const acceptOrder = (id) => {
  return request({
    url: `/merchant/order/accept/${id}`,
    method: 'post'
  })
}

export const completeOrder = (id) => {
  return request({
    url: `/merchant/orders/${id}/complete`,
    method: 'post'
  })
}

export const cancelOrder = (id) => {
  return request({
    url: `/merchant/orders/${id}/cancel`,
    method: 'post'
  })
}

export const getReviews = (params) => {
  return request({
    url: '/merchant/reviews',
    method: 'get',
    params
  })
}

export const replyReview = (id, data) => {
  return request({
    url: `/merchant/reviews/${id}/reply`,
    method: 'post',
    data
  })
}

export const changePassword = (data) => {
  return request({
    url: '/merchant/password',
    method: 'post',
    data
  })
}

export const sendSms = (data) => {
  return request({
    url: '/merchant/sms/send',
    method: 'post',
    data
  })
}

export const changePhone = (data) => {
  return request({
    url: '/merchant/phone',
    method: 'post',
    data
  })
}

// 系统公告
export const getMerchantNotices = () => {
  return request({
    url: '/notice/merchant',
    method: 'get'
  })
}

export const getNoticeDetail = (id) => {
  return request({
    url: `/notice/${id}`,
    method: 'get'
  })
}

// 聊天消息
export const getChatHistory = (userId) => {
  return request({
    url: `/chat/history/${userId}`,
    method: 'get',
    params: { withUserType: 1 }  // 只获取与用户的聊天
  })
}

export const getChatConversations = () => {
  console.log('=== 商家调用getChatConversations API ===')
  const promise = request({
    url: '/chat/conversations',
    method: 'get'
  })
  promise.then(data => {
    console.log('getChatConversations返回数据:', data)
    console.log('返回数据类型:', typeof data)
    console.log('返回数据长度:', data?.length || 0)
    if (data && data.length > 0) {
      data.forEach((item, index) => {
        console.log(`会话${index}:`, item)
      })
    }
  }).catch(err => {
    console.error('getChatConversations错误:', err)
  })
  return promise
}

export const sendChatMessage = (data) => {
  return request({
    url: '/chat/send',
    method: 'post',
    data
  })
}
