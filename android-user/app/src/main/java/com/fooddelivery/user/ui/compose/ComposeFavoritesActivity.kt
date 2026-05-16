package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.model.FavoriteMerchant
import com.fooddelivery.user.ui.compose.screens.FavoriteItem
import com.fooddelivery.user.ui.compose.screens.FavoriteType
import com.fooddelivery.user.ui.compose.screens.FavoritesScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeFavoritesActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val favoriteMerchants by viewModel.favorites.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                
                Log.d("FavoritesActivity", "onCreate: favorites.size=${favoriteMerchants.size}")
                
                val favorites: List<FavoriteItem> = remember(favoriteMerchants) {
                    favoriteMerchants.map { merchant ->
                        val favoriteType = merchant.favoriteType
                        if (favoriteType == "food") {
                            FavoriteItem(
                                id = merchant.id ?: 0L,
                                merchantId = merchant.merchantId,
                                name = merchant.foodName ?: "",
                                image = merchant.foodImage,
                                type = FavoriteType.DISH,
                                merchantName = merchant.merchantName,
                                rating = null,
                                salesVolume = null,
                                price = merchant.foodPrice?.toString(),
                                deliveryFee = null,
                                deliveryTime = null
                            )
                        } else {
                            FavoriteItem(
                                id = merchant.id ?: 0L,
                                merchantId = merchant.merchantId,
                                name = merchant.merchantName ?: "",
                                image = merchant.logo,
                                type = FavoriteType.MERCHANT,
                                merchantName = merchant.merchantName,
                                rating = merchant.rating?.toFloat(),
                                salesVolume = merchant.salesVolume,
                                price = merchant.minAmount?.toString(),
                                deliveryFee = merchant.deliveryFee?.toDouble(),
                                deliveryTime = merchant.averageDeliveryTime?.let { "${it}分钟" }
                            )
                        }
                    }
                }
                
                LaunchedEffect(Unit) {
                    viewModel.fetchFavorites()
                }
                
                FavoritesScreen(
                    favorites = favorites,
                    isLoading = isLoading,
                    onBack = { finish() },
                    onItemClick = { item ->
                        if (item.type == FavoriteType.MERCHANT && item.merchantId != null) {
                            val intent = Intent(this, ComposeProductDetailActivity::class.java)
                            intent.putExtra("merchantId", item.merchantId.toLong())
                            startActivity(intent)
                        }
                    },
                    onRemoveFavorite = { favoriteId ->
                        val merchant = favoriteMerchants.find { it.id == favoriteId }
                        if (merchant != null) {
                            if (merchant.favoriteType == "food" && merchant.foodId != null) {
                                viewModel.removeFoodFavorite(
                                    foodId = merchant.foodId,
                                    onSuccess = {
                                        viewModel.fetchFavorites()
                                    },
                                    onError = { error ->
                                        Log.e("FavoritesActivity", "Remove food favorite failed: $error")
                                    }
                                )
                            } else if (merchant.merchantId != null) {
                                viewModel.removeFavorite(
                                    merchantId = merchant.merchantId,
                                    onSuccess = {
                                        viewModel.fetchFavorites()
                                    },
                                    onError = { error ->
                                        Log.e("FavoritesActivity", "Remove merchant favorite failed: $error")
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("FavoritesActivity", "onResume: refreshing favorites")
        viewModel.fetchFavorites()
    }
}
