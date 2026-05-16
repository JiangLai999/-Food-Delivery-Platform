package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fooddelivery.user.ui.compose.screens.ProfileScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeProfileActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val user by viewModel.user.collectAsState()
                    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                    val favorites by viewModel.favorites.collectAsState()
                    val coupons by viewModel.coupons.collectAsState()
                    
                    LaunchedEffect(Unit) {
                        viewModel.refreshLoginStatus()
                        if (isLoggedIn) {
                            viewModel.fetchFavorites()
                            viewModel.fetchCoupons()
                        }
                    }
                    
                    ProfileScreen(
                        user = user,
                        isLoggedIn = isLoggedIn,
                        couponCount = coupons.size,
                        favoriteCount = favorites.size,
                        onLogout = {
                            viewModel.logout()
                            val intent = android.content.Intent(this, ComposeLoginActivity::class.java)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        },
                        onLoginClick = {
                            startActivity(android.content.Intent(this, ComposeLoginActivity::class.java))
                        },
                        onFavoritesClick = {
                            android.util.Log.d("ProfileActivity", "onFavoritesClick triggered")
                            startActivity(android.content.Intent(this, ComposeFavoritesActivity::class.java))
                        }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshLoginStatus()
    }
}
