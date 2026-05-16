package com.fooddelivery.user.utils

import android.util.Log

object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * 将相对图片路径转换为完整 URL
     */
    fun getFullImageUrl(path: String?): String {
        if (path.isNullOrBlank()) {
            return ""
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        val baseUrl = AppConfig.getImageBaseUrl()
        val cleanPath = path.trimStart('/')
        val fullUrl = "${baseUrl}${cleanPath}"
        Log.d(TAG, "getFullImageUrl: path=$path -> fullUrl=$fullUrl")
        return fullUrl
    }

    /**
     * 获取商家图片 URL
     */
    fun getMerchantImageUrl(logoPath: String?): String {
        return getFullImageUrl(logoPath)
    }

    /**
     * 获取食品图片 URL
     */
    fun getFoodImageUrl(imagePath: String?): String {
        return getFullImageUrl(imagePath)
    }
}
