package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.ui.compose.screens.*
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import com.fooddelivery.user.ui.compose.manager.CartManager

class ComposeMainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val user by viewModel.user.collectAsStateWithLifecycle()
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val hotMerchants by viewModel.hotMerchants.collectAsStateWithLifecycle()
                val recommendedMerchants by viewModel.recommendedMerchants.collectAsStateWithLifecycle()
                val orders by viewModel.orders.collectAsStateWithLifecycle()
                val addresses by viewModel.addresses.collectAsStateWithLifecycle()
                val coupons by viewModel.coupons.collectAsStateWithLifecycle()
                val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                val searchFoodResults by viewModel.searchFoodResults.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val merchantCategories by viewModel.merchantCategories.collectAsStateWithLifecycle()
                
                val cartItems by CartManager.cartItems.collectAsStateWithLifecycle()
                val cartMerchantName by CartManager.currentMerchantName.collectAsStateWithLifecycle()
                val cartDeliveryFee by CartManager.deliveryFee.collectAsStateWithLifecycle()
                val cartMinOrder by CartManager.minOrderAmount.collectAsStateWithLifecycle()
                
                LaunchedEffect(Unit) {
                    viewModel.fetchHotMerchants(50)
                    viewModel.fetchRecommendedMerchants()
                    viewModel.fetchAddresses()
                    viewModel.fetchMerchantCategories()
                    viewModel.fetchCoupons()
                    viewModel.fetchFavorites()
                }
                
                var showSearchResult by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }
                
                MainScreen(
                    user = user,
                    isLoggedIn = isLoggedIn,
                    hotMerchants = hotMerchants,
                    recommendedMerchants = recommendedMerchants,
                    merchantCategories = merchantCategories,
                    orders = orders,
                    addresses = addresses,
                    coupons = coupons,
                    favorites = favorites,
                    searchResults = searchResults,
                    searchFoodResults = searchFoodResults,
                    cartItems = cartItems,
                    cartMerchantName = cartMerchantName ?: "",
                    cartDeliveryFee = cartDeliveryFee,
                    cartMinOrderAmount = cartMinOrder,
                    cartItemCount = cartItems.sumOf { it.quantity },
                    isLoading = isLoading,
                    onNavigateToMerchant = { merchantId ->
                        val intent = Intent(this, ComposeProductDetailActivity::class.java)
                        intent.putExtra("merchantId", merchantId)
                        startActivity(intent)
                    },
                    onNavigateToSearch = {
                        showSearchResult = true
                    },
                    showSearchResult = showSearchResult,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearch = { keyword, categoryId, sort ->
                        viewModel.searchMerchants(keyword, categoryId)
                        viewModel.filterFoods(keyword, null, categoryId, sort)
                    },
                    onNavigateToCart = {
                        startActivity(Intent(this, ComposeCartActivity::class.java))
                    },
                    onNavigateToOrders = { filter: String? ->
                        val intent = Intent(this, ComposeOrderListActivity::class.java)
                        if (filter != null && filter != "all") {
                            intent.putExtra("filter", filter)
                        }
                        startActivity(intent)
                    },
                    onNavigateToAddress = {
                        startActivity(Intent(this, ComposeAddressListActivity::class.java))
                    },
                    onNavigateToCoupon = {
                        startActivity(Intent(this, ComposeCouponActivity::class.java))
                    },
                    onNavigateToWallet = {
                        startActivity(Intent(this, ComposeWalletActivity::class.java))
                    },
                    onNavigateToCustomerService = {
                        startActivity(Intent(this, ComposeCustomerServiceActivity::class.java))
                    },
                    onLogout = {
                        viewModel.logout()
                        val intent = Intent(this, ComposeLoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onFetchOrders = {
                        viewModel.fetchOrders()
                    }
                )
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "首页")
    object Search : BottomNavItem("search", Icons.Filled.Search, "搜索")
    object Orders : BottomNavItem("orders", Icons.Filled.Receipt, "订单")
    object Profile : BottomNavItem("profile", Icons.Filled.Person, "我的")
}

@Composable
fun MainScreen(
    user: com.fooddelivery.user.model.User? = null,
    isLoggedIn: Boolean = false,
    hotMerchants: List<com.fooddelivery.user.model.Merchant> = emptyList(),
    recommendedMerchants: List<com.fooddelivery.user.model.Merchant> = emptyList(),
    merchantCategories: List<com.fooddelivery.user.model.MerchantCategory> = emptyList(),
    orders: List<com.fooddelivery.user.model.Order> = emptyList(),
    addresses: List<com.fooddelivery.user.model.Address> = emptyList(),
    coupons: List<com.fooddelivery.user.model.Coupon> = emptyList(),
    favorites: List<com.fooddelivery.user.model.FavoriteMerchant> = emptyList(),
    searchResults: List<com.fooddelivery.user.model.Merchant> = emptyList(),
    searchFoodResults: List<com.fooddelivery.user.model.FoodItem> = emptyList(),
    cartItems: List<com.fooddelivery.user.model.CartItem> = emptyList(),
    cartMerchantName: String = "",
    cartDeliveryFee: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    cartMinOrderAmount: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    cartItemCount: Int = 0,
    isLoading: Boolean = false,
    onNavigateToMerchant: (Long) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    showSearchResult: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearch: (String, Long?, String?) -> Unit = { _, _, _ -> },
    onCloseSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToOrders: (String?) -> Unit = {},
    onNavigateToAddress: () -> Unit = {},
    onNavigateToCoupon: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToCustomerService: () -> Unit = {},
    onLogout: () -> Unit = {},
    onFetchOrders: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var localSearchQuery by remember { mutableStateOf("") }
    var localShowSearchResult by remember { mutableStateOf(false) }
    val items = listOf(BottomNavItem.Home, BottomNavItem.Orders, BottomNavItem.Profile)
    val context = LocalContext.current

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { 
                            if (index == 1) {
                                onFetchOrders()
                            }
                            selectedTab = index 
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (index == 1 && orders.count { it.status in listOf(0, 1, 2, 3) } > 0) {
                                        Badge(containerColor = BrandOrange) {
                                            Text(orders.count { it.status in listOf(0, 1, 2, 3) }.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = item.icon, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandOrange,
                            selectedTextColor = BrandOrange,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = BrandOrange.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (localShowSearchResult) {
                SearchResultScreen(
                    searchResults = searchResults,
                    searchFoodResults = searchFoodResults,
                    searchQuery = localSearchQuery,
                    onSearchQueryChange = { localSearchQuery = it },
                    onSearch = onSearch,
                    onBackClick = { localShowSearchResult = false },
                    onMerchantClick = { merchantId ->
                        localShowSearchResult = false
                        onNavigateToMerchant(merchantId)
                    },
                    isLoading = isLoading
                )
            } else {
                when (selectedTab) {
                    0 -> HomeScreen(
                        merchants = hotMerchants,
                        recommendedMerchants = recommendedMerchants,
                        merchantCategories = merchantCategories,
                        currentAddress = addresses.firstOrNull { it.isDefault() }?.fullAddress ?: "静安区南京西路",
                        isLoading = isLoading,
                        onNavigateToMerchant = onNavigateToMerchant,
                        onNavigateToSearch = { localShowSearchResult = true },
                        onNavigateToCart = onNavigateToCart,
                        onNavigateToNotifications = {
                            val intent = Intent(context, ComposeNotificationsActivity::class.java)
                            context.startActivity(intent)
                        },
                        onNavigateToAddressSelect = onNavigateToAddress,
                        onCategoryClick = { },
                        cartItemCount = cartItemCount
                    )
                    1 -> OrderListScreen(
                        orders = orders,
                        isLoading = isLoading,
                        onTrackOrder = { orderId ->
                            val intent = Intent(context, ComposeDeliveryTrackingActivity::class.java)
                            intent.putExtra("orderId", orderId)
                            context.startActivity(intent)
                        },
                        onReorder = { orderId ->
                            val order = orders.find { it.id == orderId }
                            val merchantId = order?.merchantId ?: order?.merchant?.id
                            if (merchantId != null && merchantId > 0) {
                                onNavigateToMerchant(merchantId)
                            } else {
                                Toast.makeText(context, "订单商家信息无效", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onReviewClick = { orderId, merchantId, hasReviewed ->
                            android.util.Log.d("MainActivity", "onReviewClick: orderId=$orderId, merchantId=$merchantId, hasReviewed=$hasReviewed")
                            if (orderId > 0) {
                                try {
                                    val intent = if (hasReviewed) {
                                        Intent(context, ComposeReviewDetailActivity::class.java)
                                    } else {
                                        Intent(context, ComposeReviewActivity::class.java)
                                    }
                                    intent.putExtra("orderId", orderId)
                                    if (!hasReviewed) {
                                        intent.putExtra("merchantId", merchantId)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "Failed to start ReviewActivity", e)
                                    Toast.makeText(context, "打开评价页面失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "订单信息无效", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    2 -> ProfileScreen(
                        user = user,
                        isLoggedIn = isLoggedIn,
                        couponCount = coupons.size,
                        favoriteCount = favorites.size,
                        footprintCount = 0,
                        onOrderClick = { filter -> onNavigateToOrders(filter) },
                        onAddressClick = onNavigateToAddress,
                        onCouponClick = onNavigateToCoupon,
                        onFavoritesClick = {
                            val intent = Intent(context, ComposeFavoritesActivity::class.java)
                            context.startActivity(intent)
                        },
                        onWalletClick = onNavigateToWallet,
                        onCustomerServiceClick = onNavigateToCustomerService,
                        onLogout = onLogout,
                        onLoginClick = {
                            val intent = Intent(context, ComposeLoginActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
