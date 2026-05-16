package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.ui.compose.screens.SearchScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeSearchActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                val searchFoodResults by viewModel.searchFoodResults.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                
                SearchScreen(
                    searchResults = searchResults,
                    searchFoodResults = searchFoodResults,
                    isLoading = isLoading,
                    onBackClick = { finish() },
                    onSearch = { keyword, categoryId, sort ->
                        viewModel.searchMerchants(keyword, categoryId)
                        viewModel.filterFoods(keyword, null, categoryId, sort)
                    },
                    onMerchantClick = { merchantId ->
                        val intent = android.content.Intent(this, ComposeProductDetailActivity::class.java)
                        intent.putExtra("merchantId", merchantId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
