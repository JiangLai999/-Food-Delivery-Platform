package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.ui.compose.screens.CartScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.manager.CartManager

class ComposeCartActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val cartItems by CartManager.cartItems.collectAsStateWithLifecycle()
                val merchantName by CartManager.currentMerchantName.collectAsStateWithLifecycle()
                val deliveryFee by CartManager.deliveryFee.collectAsStateWithLifecycle()
                val minOrderAmount by CartManager.minOrderAmount.collectAsStateWithLifecycle()
                
                CartScreen(
                    cartItems = cartItems,
                    merchantName = merchantName ?: "",
                    deliveryFee = deliveryFee,
                    minOrderAmount = minOrderAmount,
                    onQuantityChange = { foodId, quantity ->
                        CartManager.updateQuantity(foodId, quantity)
                    },
                    onClearCart = {
                        CartManager.clearCart()
                        Toast.makeText(this, "购物车已清空", Toast.LENGTH_SHORT).show()
                    },
                    onNavigateToCheckout = {
                        if (cartItems.isNotEmpty()) {
                            startActivity(Intent(this, ComposeCheckoutActivity::class.java))
                        } else {
                            Toast.makeText(this, "购物车为空", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onContinueShopping = {
                        startActivity(Intent(this, ComposeMainActivity::class.java))
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
