package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeExpiredCouponsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val context = LocalContext.current
                
                com.fooddelivery.user.ui.compose.screens.ExpiredCouponsScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}
