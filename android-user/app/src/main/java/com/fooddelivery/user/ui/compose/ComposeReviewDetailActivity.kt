package com.fooddelivery.user.ui.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.fooddelivery.user.model.Review
import com.fooddelivery.user.network.ApiService
import com.fooddelivery.user.network.ApiClient
import com.fooddelivery.user.ui.compose.screens.ReviewDetailScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComposeReviewDetailActivity : ComponentActivity() {
    private lateinit var apiService: ApiService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        apiService = ApiClient.getInstance().apiService
        
        val orderId = intent.getLongExtra("orderId", 0L)
        val merchantName = intent.getStringExtra("merchantName") ?: ""
        
        if (orderId == 0L) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        var review by mutableStateOf<Review?>(null)
        
        apiService.getReviewByOrderId(orderId).enqueue(object : Callback<com.fooddelivery.user.model.Result<Review>> {
            override fun onResponse(call: Call<com.fooddelivery.user.model.Result<Review>>, response: Response<com.fooddelivery.user.model.Result<Review>>) {
                val result = response.body()
                if (result != null && result.isSuccess && result.data != null) {
                    review = result.data
                } else {
                    Toast.makeText(this@ComposeReviewDetailActivity, "获取评价失败", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<com.fooddelivery.user.model.Result<Review>>, t: Throwable) {
                Toast.makeText(this@ComposeReviewDetailActivity, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
        
        setContent {
            FoodDeliveryTheme {
                ReviewDetailScreen(
                    review = review,
                    orderId = orderId,
                    merchantName = merchantName,
                    onBack = { finish() }
                )
            }
        }
    }
}
