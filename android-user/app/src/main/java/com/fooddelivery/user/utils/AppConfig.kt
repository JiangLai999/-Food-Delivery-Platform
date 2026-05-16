package com.fooddelivery.user.utils

import android.content.Context
import com.fooddelivery.user.R

object AppConfig {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getApiBaseUrl(): String {
        return context?.getString(R.string.api_base_url) ?: "http://10.0.2.2:8080/api/"
    }

    fun getServerIp(): String {
        val baseUrl = getApiBaseUrl()
        val regex = """https?://([^:/]+)""".toRegex()
        val match = regex.find(baseUrl)
        return match?.groupValues?.get(1) ?: "10.0.2.2"
    }

    fun getServerPort(): Int {
        val baseUrl = getApiBaseUrl()
        val regex = """:(\d+)/""".toRegex()
        val match = regex.find(baseUrl)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 8080
    }

    fun getImageBaseUrl(): String {
        return getApiBaseUrl()
    }

    fun getWebSocketUrl(userId: Long): String {
        val ip = getServerIp()
        val port = getServerPort()
        return "ws://$ip:$port/api/native-ws/$userId"
    }
}
