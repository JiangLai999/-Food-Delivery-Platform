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

class ComposeAdminChatActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var adminId: Long = 1L  // 管理员ID，默认为1
    private var adminName: String = "客服"
    private var userId: Long = 0L
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        adminName = intent.getStringExtra("adminName") ?: "客服"
        
        userId = SPUtils.getLong("userId", 0L)
        
        Log.d("AdminChat", "userId=$userId, adminId=$adminId")
        
        // 连接WebSocket
        if (userId > 0) {
            webSocketManager.connect(userId)
        }
        
        // 加载聊天历史
        viewModel.fetchCustomerServiceChatHistory()
        
        setContent {
            FoodDeliveryTheme {
                val messages by viewModel.customerChatMessages.collectAsState()
                
                LaunchedEffect(messages) {
                    Log.d("AdminChat", "Messages updated: ${messages.size}")
                }
                
                MerchantChatScreen(
                    merchantName = adminName,
                    messages = messages,
                    onBack = { finish() },
                    onSendMessage = { content ->
                        sendMessage(content)
                    }
                )
            }
        }
    }
    
    private fun sendMessage(content: String) {
        viewModel.sendCustomerServiceMessage(
            content = content,
            onSuccess = {
                Log.d("AdminChat", "Message sent successfully")
            },
            onError = { error ->
                Log.e("AdminChat", "Failed to send message: $error")
            }
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.disconnect()
    }
}
