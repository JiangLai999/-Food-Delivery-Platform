package com.fooddelivery.user.ui.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.fooddelivery.user.model.CreateReviewRequest
import com.fooddelivery.user.model.Order
import com.fooddelivery.user.model.OrderItem
import com.fooddelivery.user.network.ApiService
import com.fooddelivery.user.network.ApiClient
import com.fooddelivery.user.ui.compose.screens.ReviewScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComposeReviewActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var apiService: ApiService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        android.util.Log.d("ReviewActivity", "onCreate started")
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        apiService = ApiClient.getInstance().apiService
        
        val orderId = intent.getLongExtra("orderId", 0L)
        val merchantId = intent.getLongExtra("merchantId", 0L)
        
        android.util.Log.d("ReviewActivity", "onCreate: orderId=$orderId, merchantId=$merchantId")
        
        if (orderId == 0L || merchantId == 0L) {
            android.util.Log.e("ReviewActivity", "Invalid parameters: orderId=$orderId, merchantId=$merchantId")
            Toast.makeText(this, "参数错误: orderId=$orderId, merchantId=$merchantId", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // 获取订单详情
        var orderItems by mutableStateOf<List<OrderItem>>(emptyList())
        var orderTotal by mutableStateOf("¥0")
        var isLoading by mutableStateOf(true)
        
        apiService.getOrderDetail(orderId).enqueue(object : Callback<com.fooddelivery.user.model.Result<Order>> {
            override fun onResponse(call: Call<com.fooddelivery.user.model.Result<Order>>, response: Response<com.fooddelivery.user.model.Result<Order>>) {
                isLoading = false
                val result = response.body()
                if (result != null && result.isSuccess && result.data != null) {
                    val order = result.data
                    orderItems = order.items ?: emptyList()
                    val displayPrice = order.finalAmount ?: order.totalAmount ?: java.math.BigDecimal.ZERO
                    orderTotal = "¥$displayPrice"
                    android.util.Log.d("ReviewActivity", "Order items: ${orderItems.size}, finalAmount=${order.finalAmount}, totalAmount=${order.totalAmount}, displayPrice=$displayPrice")
                } else {
                    android.util.Log.e("ReviewActivity", "Failed to get order: ${result?.message}")
                }
            }
            
            override fun onFailure(call: Call<com.fooddelivery.user.model.Result<Order>>, t: Throwable) {
                isLoading = false
                android.util.Log.e("ReviewActivity", "Failed to get order: ${t.message}")
            }
        })
        
        setContent {
            FoodDeliveryTheme {
                ReviewScreen(
                    orderId = orderId.toString(),
                    orderItems = orderItems,
                    orderTotal = orderTotal,
                    isLoading = isLoading,
                    onBack = { finish() },
                    onSubmit = { },
                    onSubmitReview = { tasteRating: Int, portionRating: Int, content: String, images: List<String>, isAnonymous: Boolean ->
                        if (tasteRating <= 0) {
                            Toast.makeText(this@ComposeReviewActivity, "请选择评分", Toast.LENGTH_SHORT).show()
                            return@ReviewScreen
                        }
                        
                        val request = CreateReviewRequest().apply {
                            this.orderId = orderId
                            this.merchantId = merchantId
                            this.tasteRating = tasteRating
                            this.portionRating = portionRating
                            this.rating = tasteRating
                            this.content = content
                            this.images = images
                            this.isAnonymous = isAnonymous
                        }
                        
                        viewModel.createReview(
                            request = request,
                            onSuccess = { message ->
                                Toast.makeText(this@ComposeReviewActivity, message, Toast.LENGTH_SHORT).show()
                                finish()
                            },
                            onError = { error: String ->
                                if (error.contains("已评价")) {
                                    Toast.makeText(this@ComposeReviewActivity, "该订单已评价", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@ComposeReviewActivity, error, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
