package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.fooddelivery.user.ui.compose.screens.WalletScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme

class ComposeWalletActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodDeliveryTheme {
                WalletScreen(
                    onRecharge = {
                        startActivity(android.content.Intent(this, ComposeRechargeActivity::class.java))
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
