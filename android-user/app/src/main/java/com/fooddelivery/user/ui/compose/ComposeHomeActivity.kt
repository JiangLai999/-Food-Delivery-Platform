package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fooddelivery.user.ui.compose.screens.HomeScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme

class ComposeHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodDeliveryTheme {
                HomeScreen(
                        merchants = emptyList(),
                    isLoading = false,
                    onNavigateToMerchant = { merchantId ->
                        val intent = android.content.Intent(this, ComposeProductDetailActivity::class.java)
                        intent.putExtra("merchantId", merchantId)
                        startActivity(intent)
                    },
                    onNavigateToSearch = {
                        startActivity(android.content.Intent(this, ComposeSearchActivity::class.java))
                    },
                    onNavigateToNotifications = {},
                    onNavigateToCart = {
                        startActivity(android.content.Intent(this, ComposeCartActivity::class.java))
                    },
                    cartItemCount = 0
                )
            }
        }
    }
}
