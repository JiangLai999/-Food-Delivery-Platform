package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fooddelivery.user.ui.compose.screens.NotificationsScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeNotificationsActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        viewModel.fetchSystemNotices()
        
        setContent {
            FoodDeliveryTheme {
                val systemNotices by viewModel.systemNotices.collectAsState()
                
                NotificationsScreen(
                    systemNotices = systemNotices,
                    onBack = { finish() },
                    onNotificationClick = { },
                    onMarkAllRead = { }
                )
            }
        }
    }
}
