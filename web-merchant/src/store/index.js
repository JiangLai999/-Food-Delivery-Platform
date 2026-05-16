import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('merchant_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('merchant_userInfo') || '{}'))
  const merchantInfo = ref(JSON.parse(localStorage.getItem('merchant_info') || '{}'))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('merchant_token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('merchant_userInfo', JSON.stringify(info))
  }

  const setMerchantInfo = (info) => {
    merchantInfo.value = info
    localStorage.setItem('merchant_info', JSON.stringify(info))
  }

  const clearAuth = () => {
    token.value = ''
    userInfo.value = {}
    merchantInfo.value = {}
    localStorage.removeItem('merchant_token')
    localStorage.removeItem('merchant_userInfo')
    localStorage.removeItem('merchant_info')
  }

  return {
    token,
    userInfo,
    merchantInfo,
    setToken,
    setUserInfo,
    setMerchantInfo,
    clearAuth
  }
})
