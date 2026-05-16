package com.fooddelivery.user.ui.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fooddelivery.user.model.ChatMessage
import com.fooddelivery.user.ui.compose.screens.CustomerServiceScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import com.fooddelivery.user.utils.SPUtils
import com.fooddelivery.user.utils.WebSocketManager
import org.json.JSONObject

class ComposeCustomerServiceActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var adminId: Long = 1L  // 管理员ID，默认为1
    private var userId: Long = 0L
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        userId = SPUtils.getLong("userId", 0L)
        
        Log.d("CustomerService", "userId=$userId, adminId=$adminId")
        
        // 连接WebSocket
        if (userId > 0) {
            webSocketManager.connect(userId)
            
            // 添加消息监听器
            object : WebSocketManager.MessageListener {
                override fun onMessage(message: String) {
                    Log.d("CustomerService", "收到WebSocket消息: $message")
                    try {
                        val json = JSONObject(message)
                        val type = json.optString("type")
                        if (type == "CHAT_MESSAGE") {
                            val data = json.optJSONObject("data")
                            if (data != null && data.optLong("fromUserId") == adminId) {
                                // 收到管理员的消息，刷新聊天记录
                                viewModel.fetchCustomerServiceChatHistory()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CustomerService", "解析WebSocket消息失败", e)
                    }
                }
                
                override fun onConnected() {
                    Log.d("CustomerService", "WebSocket已连接")
                }
                
                override fun onDisconnected() {
                    Log.d("CustomerService", "WebSocket已断开")
                }
                
                override fun onError(error: String) {
                    Log.e("CustomerService", "WebSocket错误: $error")
                }
            }.also {
                webSocketManager.addMessageListener(it)
                lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
                    override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                        webSocketManager.removeMessageListener(it)
                    }
                })
            }
        }
        
        setContent {
            FoodDeliveryTheme {
                CustomerServiceScreen(
                    onBack = { finish() },
                    viewModel = viewModel
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 页面恢复时刷新聊天记录
        viewModel.fetchCustomerServiceChatHistory()
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}
