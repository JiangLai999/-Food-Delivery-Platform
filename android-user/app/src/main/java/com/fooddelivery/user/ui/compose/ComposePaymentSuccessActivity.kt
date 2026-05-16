package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fooddelivery.user.ui.compose.screens.PaymentSuccessScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme

class ComposePaymentSuccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val orderId = intent.getStringExtra("orderId") ?: ""
        val amount = intent.getDoubleExtra("amount", 0.0)
        setContent {
            FoodDeliveryTheme {
                PaymentSuccessScreen(
                    orderId = orderId,
                    amount = if (amount > 0) java.math.BigDecimal.valueOf(amount) else java.math.BigDecimal.ZERO,
                    onNavigateToOrder = {
                        val orderIdLong = orderId.toLongOrNull() ?: 0L
                        startActivity(android.content.Intent(this, ComposeDeliveryTrackingActivity::class.java).apply {
                            putExtra("orderId", orderIdLong)
                        })
                        finish()
                    },
                    onNavigateToHome = {
                        val intent = android.content.Intent(this, ComposeMainActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
