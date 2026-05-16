package com.fooddelivery.user.utils

import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit
import com.fooddelivery.user.model.ChatMessage
import org.json.JSONObject

class WebSocketManager private constructor() {

    private var webSocket: WebSocket? = null
    private var userId: Long = -1
    private var isConnected = false
    private val messageListeners = mutableListOf<MessageListener>()
    private val chatListeners = mutableListOf<ChatMessageListener>()

    interface MessageListener {
        fun onConnected()
        fun onDisconnected()
        fun onMessage(message: String)
        fun onError(error: String)
    }

    interface ChatMessageListener {
        fun onChatMessage(message: ChatMessage)
    }

    companion object {
        private const val TAG = "WebSocketManager"
        private const val RECONNECT_INTERVAL = 5000L

        @Volatile
        private var instance: WebSocketManager? = null

        fun getInstance(): WebSocketManager {
            return instance ?: synchronized(this) {
                instance ?: WebSocketManager().also { instance = it }
            }
        }
    }

    fun connect(userId: Long) {
        this.userId = userId
        if (isConnected) {
            return
        }

        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .pingInterval(15, TimeUnit.SECONDS)
            .build()

        val wsUrl = AppConfig.getWebSocketUrl(userId)
        Log.d(TAG, "Connecting to WebSocket: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnected = true
                messageListeners.forEach { it.onConnected() }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received: $text")
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    
                    when (type) {
                         "CHAT_MESSAGE" -> {
                            val data = json.optJSONObject("data")
                            if (data != null) {
                                val message = com.fooddelivery.user.model.ChatMessage()
                                message.id = data.optLong("id")
                                message.fromUserId = data.optLong("fromUserId")
                                message.fromUserType = data.optInt("fromUserType")
                                message.toUserId = data.optLong("toUserId")
                                message.toUserType = data.optInt("toUserType")
                                message.content = data.optString("content")
                                // 时间戳直接作为字符串保存
                                message.createTime = data.optString("timestamp")
                                chatListeners.forEach { it.onChatMessage(message) }
                            }
                        }
                        "RIDER_LOCATION", "ORDER_STATUS" -> {
                            messageListeners.forEach { it.onMessage(text) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parse message error", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Closing: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Closed: $code $reason")
                isConnected = false
                messageListeners.forEach { it.onDisconnected() }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Error: ${t.message}")
                isConnected = false
                messageListeners.forEach { it.onError(t.message ?: "Unknown error") }
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (!isConnected && userId > 0) {
                        connect(userId)
                    }
                }, RECONNECT_INTERVAL)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnect")
        webSocket = null
        isConnected = false
    }

    fun subscribeOrder(orderId: Long) {
        sendMessage("""{"type":"SUBSCRIBE_ORDER","orderId":$orderId}""")
    }

    fun unsubscribeOrder() {
        sendMessage("""{"type":"UNSUBSCRIBE_ORDER"}""")
    }

    fun sendChatMessage(toUserId: Long, content: String, contentType: Int = 0) {
        val message = JSONObject().apply {
            put("type", "CHAT_MESSAGE")
            put("toUserId", toUserId)
            put("content", content)
            put("contentType", contentType)
        }
        sendMessage(message.toString())
    }

    private fun sendMessage(message: String) {
        if (isConnected) {
            webSocket?.send(message)
        } else {
            Log.w(TAG, "WebSocket not connected, message not sent: $message")
        }
    }

    fun addMessageListener(listener: MessageListener) {
        messageListeners.add(listener)
    }

    fun removeMessageListener(listener: MessageListener) {
        messageListeners.remove(listener)
    }

    fun addChatListener(listener: ChatMessageListener) {
        chatListeners.add(listener)
    }

    fun removeChatListener(listener: ChatMessageListener) {
        chatListeners.remove(listener)
    }

    fun isConnected(): Boolean = isConnected
}
