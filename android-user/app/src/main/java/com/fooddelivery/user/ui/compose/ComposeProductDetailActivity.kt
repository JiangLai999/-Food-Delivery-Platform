package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.model.FoodItem
import com.fooddelivery.user.ui.compose.screens.MerchantDetailScreen
import com.fooddelivery.user.ui.compose.screens.ProductDetailScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import com.fooddelivery.user.ui.compose.manager.CartManager
import com.fooddelivery.user.utils.SPUtils

class ComposeProductDetailActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var isFavorite by mutableStateOf(false)
    private var favoriteFoodIds by mutableStateOf(setOf<Long>())
    private var merchantId: Long = -1L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        merchantId = intent.getLongExtra("merchantId", -1L)
        android.util.Log.d("ProductDetail", "onCreate: merchantId=$merchantId")
        
        setContent {
            FoodDeliveryTheme {
                val merchant by viewModel.currentMerchant.collectAsStateWithLifecycle()
                val foods by viewModel.merchantFoods.collectAsStateWithLifecycle()
                val reviews by viewModel.merchantReviews.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                
                android.util.Log.d("ProductDetail", "merchant=${merchant?.name}, foods=${foods.size}, reviews=${reviews.size}, isLoading=$isLoading")
                
                val cartItems by CartManager.cartItems.collectAsStateWithLifecycle()
                val cartMerchantId by CartManager.currentMerchantId.collectAsStateWithLifecycle()
                
                var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
                
                fun checkFavoriteStatus() {
                    if (merchantId > 0 && SPUtils.getLong("userId", -1) > 0) {
                        viewModel.checkFavorite(merchantId) { result ->
                            isFavorite = result
                        }
                    }
                }
                
                fun checkFoodFavorites() {
                    if (SPUtils.getLong("userId", -1) > 0) {
                        foods.forEach { food ->
                            food.id?.let { foodId ->
                                viewModel.checkFoodFavorite(foodId) { isFav ->
                                    if (isFav) {
                                        favoriteFoodIds = favoriteFoodIds + foodId
                                    }
                                }
                            }
                        }
                    }
                }
                
                LaunchedEffect(merchantId) {
                    android.util.Log.d("ProductDetail", "LaunchedEffect: merchantId=$merchantId")
                    if (merchantId > 0) {
                        viewModel.fetchMerchantDetail(merchantId)
                        checkFavoriteStatus()
                        viewModel.fetchMerchantReviews(merchantId)
                    } else {
                        android.util.Log.e("ProductDetail", "Invalid merchantId!")
                    }
                }
                
                LaunchedEffect(foods) {
                    if (foods.isNotEmpty()) {
                        checkFoodFavorites()
                    }
                }
                
                val currentCartItems = remember(cartItems, cartMerchantId, merchantId) {
                    if (cartMerchantId == merchantId) cartItems else emptyList()
                }
                
                if (merchantId <= 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "商家ID无效: $merchantId",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                } else if (selectedFood != null) {
                    ProductDetailScreen(
                        food = selectedFood,
                        merchant = merchant,
                        currentQuantity = currentCartItems.find { it.foodId == selectedFood?.id }?.quantity ?: 0,
                        onBack = { selectedFood = null },
                        onAddToCart = { quantity ->
                            val food = selectedFood
                            val currentMerchant = merchant
                            
                             if (food != null && currentMerchant != null) {
                                CartManager.setMerchantInfo(
                                    merchantId = currentMerchant.id ?: merchantId,
                                    merchantName = currentMerchant.name ?: "",
                                    deliveryFee = currentMerchant.deliveryFee ?: java.math.BigDecimal.ZERO,
                                    minOrderAmount = currentMerchant.minAmount ?: java.math.BigDecimal.ZERO
                                )
                                CartManager.addItem(
                                    foodId = food.id ?: 0L,
                                    foodName = food.name ?: "",
                                    foodImage = food.image,
                                    price = food.price ?: java.math.BigDecimal.ZERO,
                                    quantity = quantity
                                )
                                Toast.makeText(this@ComposeProductDetailActivity, "已加入购物车", Toast.LENGTH_SHORT).show()
                                selectedFood = null
                            }
                        }
                    )
                } else if (merchant == null && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "商家信息加载失败",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Text(
                                "merchantId = $merchantId",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    MerchantDetailScreen(
                        merchant = merchant,
                        foods = foods,
                        reviews = reviews,
                        cartItems = currentCartItems,
                        isLoading = isLoading,
                        isFavorite = isFavorite,
                        favoriteFoodIds = favoriteFoodIds,
                        onBack = { finish() },
                        onFoodClick = { food -> selectedFood = food },
                        onAddToCart = { food, quantity ->
                            val currentMerchant = merchant
                            
                            if (currentMerchant != null) {
                                CartManager.setMerchantInfo(
                                    merchantId = currentMerchant.id ?: merchantId,
                                    merchantName = currentMerchant.name ?: "",
                                    deliveryFee = currentMerchant.deliveryFee ?: java.math.BigDecimal.ZERO,
                                    minOrderAmount = currentMerchant.minAmount ?: java.math.BigDecimal.ZERO
                                )
                                CartManager.addItem(
                                    foodId = food.id ?: 0L,
                                    foodName = food.name ?: "",
                                    foodImage = food.image,
                                    price = food.price ?: java.math.BigDecimal.ZERO,
                                    quantity = quantity
                                )
                                Toast.makeText(this@ComposeProductDetailActivity, "已加入购物车", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onNavigateToCart = {
                            startActivity(Intent(this@ComposeProductDetailActivity, ComposeCartActivity::class.java))
                        },
                        onChatClick = {
                            startActivity(Intent(this@ComposeProductDetailActivity, ComposeMerchantChatActivity::class.java).apply {
                                putExtra("merchantId", merchantId)
                                putExtra("merchantName", merchant?.name)
                            })
                        },
                        onMerchantFavoriteClick = {
                            val userId = SPUtils.getLong("userId", -1)
                            android.util.Log.e("FAVORITE_DEBUG", "===== 收藏按钮被点击 =====")
                            android.util.Log.e("FAVORITE_DEBUG", "userId=$userId, isFavorite=$isFavorite, merchantId=$merchantId")
                            if (userId <= 0) {
                                android.util.Log.e("FAVORITE_DEBUG", "用户未登录，显示提示")
                                Toast.makeText(this@ComposeProductDetailActivity, "请先登录 (userId=$userId)", Toast.LENGTH_LONG).show()
                                return@MerchantDetailScreen
                            }
                            android.util.Log.e("FAVORITE_DEBUG", "用户已登录，准备调用API")
                            if (isFavorite) {
                                viewModel.removeFavorite(merchantId,
                                    onSuccess = {
                                        isFavorite = false
                                        Toast.makeText(this@ComposeProductDetailActivity, "已取消收藏", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(this@ComposeProductDetailActivity, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                viewModel.addFavorite(merchantId,
                                    onSuccess = {
                                        isFavorite = true
                                        Toast.makeText(this@ComposeProductDetailActivity, "收藏成功", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(this@ComposeProductDetailActivity, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        onFoodFavoriteClick = { food, shouldFavorite ->
                            if (SPUtils.getLong("userId", -1) <= 0) {
                                Toast.makeText(this@ComposeProductDetailActivity, "请先登录", Toast.LENGTH_SHORT).show()
                                return@MerchantDetailScreen
                            }
                            val foodId = food.id ?: return@MerchantDetailScreen
                            if (shouldFavorite) {
                                viewModel.addFoodFavorite(foodId,
                                    onSuccess = {
                                        favoriteFoodIds = favoriteFoodIds + foodId
                                        Toast.makeText(this@ComposeProductDetailActivity, "收藏成功", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(this@ComposeProductDetailActivity, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                viewModel.removeFoodFavorite(foodId,
                                    onSuccess = {
                                        favoriteFoodIds = favoriteFoodIds - foodId
                                        Toast.makeText(this@ComposeProductDetailActivity, "已取消收藏", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(this@ComposeProductDetailActivity, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
