package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.ui.compose.screens.OrderListScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeOrderListActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val filter = intent.getStringExtra("filter")
        android.util.Log.d("OrderList", "onCreate: filter=$filter")
        
        setContent {
            FoodDeliveryTheme {
                val orders by viewModel.orders.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                
                Log.d("OrderList", "onCreate: orders.size=${orders.size}, filter=$filter")
                
                LaunchedEffect(Unit) {
                    viewModel.fetchOrders()
                }
                
                Log.d("OrderList", "Before render: orders.size=${orders.size}")
                orders.take(3).forEach { order ->
                    Log.d("OrderList", "Order[${order.id}]: merchantId=${order.merchantId}, merchantName=${order.merchantName}, items=${order.items?.size}")
                }
                
                OrderListScreen(
                    orders = orders,
                    isLoading = isLoading,
                    initialFilter = filter,
                    onBackClick = { finish() },
                    onTrackOrder = { orderId ->
                        Log.d("OrderList", "onTrackOrder: orderId=$orderId")
                        val intent = Intent(this, ComposeDeliveryTrackingActivity::class.java)
                        intent.putExtra("orderId", orderId)
                        startActivity(intent)
                    },
                    onReorder = { orderId ->
                        Log.d("OrderList", "onReorder called: orderId=$orderId")
                        val order = orders.find { it.id == orderId }
                        Log.d("OrderList", "Found order: $order")
                        if (order != null) {
                            Log.d("OrderList", "Order details: id=${order.id}, merchantId=${order.merchantId}, merchant=${order.merchant}")
                            val merchantId = order.merchantId ?: order.merchant?.id
                            Log.d("OrderList", "Final merchantId = $merchantId")
                            
                            if (merchantId != null && merchantId > 0) {
                                Log.d("OrderList", "Navigating to merchant: $merchantId")
                                val intent = Intent(this, ComposeProductDetailActivity::class.java)
                                intent.putExtra("merchantId", merchantId)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "商家信息无效: merchantId=$merchantId, orderId=$orderId", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "订单不存在 orderId=$orderId", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOrderDetail = { orderId ->
                        val intent = Intent(this, ComposeDeliveryTrackingActivity::class.java)
                        intent.putExtra("orderId", orderId)
                        startActivity(intent)
                    },
                    onCancelOrder = { orderId ->
                        Log.d("OrderList", "onCancelOrder called: orderId=$orderId")
                        viewModel.cancelOrder(
                            orderId = orderId,
                            reason = "用户取消",
                            onSuccess = {
                                Log.d("OrderList", "Cancel order SUCCESS for orderId=$orderId")
                                Toast.makeText(this, "订单已取消", Toast.LENGTH_SHORT).show()
                                viewModel.fetchOrders()
                            },
                            onError = { error ->
                                Log.e("OrderList", "Cancel order FAILED: $error")
                                Toast.makeText(this, "取消失败: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onConfirmOrder = { orderId ->
                        viewModel.confirmOrder(
                            orderId = orderId,
                            onSuccess = {
                                viewModel.fetchOrders()
                            },
                            onError = { }
                        )
                    },
                    onReviewClick = { orderId, merchantId, hasReviewed ->
                        Log.d("OrderList", "onReviewClick: orderId=$orderId, merchantId=$merchantId, hasReviewed=$hasReviewed")
                        if (orderId > 0) {
                            try {
                                val intent = if (hasReviewed) {
                                    Intent(this, ComposeReviewDetailActivity::class.java)
                                } else {
                                    Intent(this, ComposeReviewActivity::class.java)
                                }
                                intent.putExtra("orderId", orderId)
                                if (!hasReviewed) {
                                    intent.putExtra("merchantId", merchantId)
                                }
                                Log.d("OrderList", "Starting ReviewActivity with intent: $intent")
                                startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("OrderList", "Failed to start ReviewActivity", e)
                                Toast.makeText(this, "打开评价页面失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "订单信息无效 orderId=$orderId", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}
