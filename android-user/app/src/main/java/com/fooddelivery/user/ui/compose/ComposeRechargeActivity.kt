package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fooddelivery.user.ui.compose.screens.RechargeScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme

class ComposeRechargeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodDeliveryTheme {
                RechargeScreen(
                    onBack = { finish() },
                    onRechargeSuccess = { finish() }
                )
            }
        }
    }
}
