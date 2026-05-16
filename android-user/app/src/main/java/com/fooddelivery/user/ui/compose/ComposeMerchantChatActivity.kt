package com.fooddelivery.user.ui.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.fooddelivery.user.model.ChatMessage
import com.fooddelivery.user.ui.compose.screens.MerchantChatScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import com.fooddelivery.user.utils.SPUtils
import com.fooddelivery.user.utils.WebSocketManager
import org.json.JSONObject

class ComposeMerchantChatActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var merchantName: String = "商家客服"
    private var merchantId: Long = 0L
    private var userId: Long = 0L
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        merchantName = intent.getStringExtra("merchantName") ?: "商家客服"
        merchantId = intent.getLongExtra("merchantId", 0L)
        userId = SPUtils.getLong("userId", 0L)
        
        // 连接WebSocket
        if (userId > 0) {
            webSocketManager.connect(userId)
        }
        
        // 加载聊天历史
        if (merchantId > 0) {
            viewModel.fetchChatHistory(merchantId)
        }
        
        setContent {
            FoodDeliveryTheme {
                val messages by viewModel.chatMessages.collectAsState()
                
                LaunchedEffect(messages) {
                    Log.d("MerchantChat", "Messages updated: ${messages.size}")
                }
                
                MerchantChatScreen(
                    merchantName = merchantName,
                    messages = messages,
                    onBack = { finish() },
                    onSendMessage = { content ->
                        sendMessage(content)
                    },
                    onReceiveMerchantMessage = { reply ->
                        // 自动回复已通过本地状态处理
                        Log.d("MerchantChat", "Auto reply: $reply")
                    }
                )
            }
        }
    }
    
    private fun sendMessage(content: String) {
        viewModel.sendChatMessage(
            toUserId = merchantId,
            content = content,
            onSuccess = {
                Log.d("MerchantChat", "Message sent successfully")
            },
            onError = { error ->
                Log.e("MerchantChat", "Failed to send message: $error")
            }
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.disconnect()
    }
}
