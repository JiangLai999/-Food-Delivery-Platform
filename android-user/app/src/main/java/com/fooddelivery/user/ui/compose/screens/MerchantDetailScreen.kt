package com.fooddelivery.user.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.fooddelivery.user.model.FoodItem
import com.fooddelivery.user.model.Merchant
import com.fooddelivery.user.model.Review
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.ui.compose.components.LoadingIndicator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDetailScreen(
    merchant: Merchant? = null,
    foods: List<FoodItem> = emptyList(),
    reviews: List<Review> = emptyList(),
    cartItems: List<com.fooddelivery.user.model.CartItem> = emptyList(),
    isLoading: Boolean = false,
    isFavorite: Boolean = false,
    favoriteFoodIds: Set<Long> = emptySet(),
    onBack: () -> Unit = {},
    onFoodClick: (FoodItem) -> Unit = {},
    onAddToCart: (FoodItem, Int) -> Unit = { _, _ -> },
    onNavigateToCart: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onMerchantFavoriteClick: () -> Unit = {},
    onFoodFavoriteClick: (FoodItem, Boolean) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("菜单", "评价", "商家")
    val selectedCategory = remember { mutableStateOf<String?>(null) }
    
    val categories = remember(foods) {
        foods.mapNotNull { it.categoryName }.distinct()
    }
    
    val filteredFoods = remember(foods, selectedCategory.value) {
        if (selectedCategory.value == null) foods
        else foods.filter { it.categoryName == selectedCategory.value }
    }
    
    val cartTotalAmount = remember(cartItems) {
        cartItems.fold(BigDecimal.ZERO) { total, item ->
            total.add(item.getSubtotal())
        }
    }
    
    val cartTotalQuantity = cartItems.sumOf { it.quantity }
    
    val deliveryFee = merchant?.deliveryFee ?: BigDecimal.ZERO
    val minAmount = merchant?.minAmount ?: BigDecimal.ZERO

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (cartTotalQuantity > 0) 100.dp else 16.dp)
        ) {
            item {
                RestaurantHeader(
                    merchant = merchant,
                    onBack = onBack,
                    onChatClick = onChatClick,
                    isFavorite = isFavorite,
                    onMerchantFavoriteClick = onMerchantFavoriteClick
                )
            }
            
            item {
                RestaurantInfoCard(
                    merchant = merchant,
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onTabSelected = { selectedTab = it }
                )
            }
            
            when (selectedTab) {
                0 -> {
                    if (categories.isNotEmpty()) {
                        item {
                            CategoryFilterRow(
                                categories = categories,
                                selectedCategory = selectedCategory.value,
                                onCategorySelected = { selectedCategory.value = it }
                            )
                        }
                    }
                    
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingIndicator(color = brandOrange)
                            }
                        }
                    } else {
                        items(filteredFoods) { food ->
                            MenuItemCard(
                                food = food,
                                cartQuantity = cartItems.find { it.foodId == food.id }?.quantity ?: 0,
                                isFavorite = favoriteFoodIds.contains(food.id),
                                onClick = { onFoodClick(food) },
                                onAddToCart = { onAddToCart(food, 1) },
                                onFavoriteClick = { onFoodFavoriteClick(food, !favoriteFoodIds.contains(food.id)) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                1 -> {
                    item {
                        ReviewsSection(
                            reviews = reviews,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                2 -> {
                    item {
                        MerchantInfoSection(
                            merchant = merchant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
        
        if (cartTotalQuantity > 0) {
            FloatingCartBar(
                totalAmount = cartTotalAmount,
                quantity = cartTotalQuantity,
                deliveryFee = deliveryFee,
                minAmount = minAmount,
                onClick = onNavigateToCart,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun RestaurantHeader(
    merchant: Merchant?,
    onBack: () -> Unit,
    onChatClick: () -> Unit = {},
    isFavorite: Boolean = false,
    onMerchantFavoriteClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AsyncImage(
                model = ImageUtils.getFullImageUrl(merchant?.banner ?: merchant?.logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                ))
        )
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onChatClick,
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "在线咨询", tint = Color.White)
            }
            IconButton(
                onClick = { },
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            ) {
                Icon(Icons.Filled.Share, contentDescription = "分享", tint = Color.White)
            }
            IconButton(
                onClick = onMerchantFavoriteClick,
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            ) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, 
                    contentDescription = "收藏", 
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        }
    }
}

@Composable
private fun RestaurantInfoCard(
    merchant: Merchant?,
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    val deliveryFee = merchant?.deliveryFee ?: BigDecimal.ZERO
    val minAmount = merchant?.minAmount ?: BigDecimal.ZERO
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-40).dp)
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                merchant?.name ?: "商家名称",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = brandOrange, modifier = Modifier.size(18.dp))
                Text(
                    " ${(merchant?.rating ?: 4.8)}",
                    fontWeight = FontWeight.Bold,
                    color = brandOrange
                )
                Text(
                    " | 月售${merchant?.salesVolume ?: 0}+",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                Text(" ${merchant?.averageDeliveryTime ?: 30}分钟", fontSize = 13.sp, color = TextGray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = brandOrange.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (deliveryFee > BigDecimal.ZERO) "¥$deliveryFee" else "免",
                            fontSize = 11.sp,
                            color = brandOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("配送费", fontSize = 12.sp, color = TextGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "¥$minAmount",
                            fontSize = 11.sp,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("起送", fontSize = 12.sp, color = TextGray)
                }
            }
            
            merchant?.notice?.takeIf { it.isNotBlank() }?.let { notice ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = brandOrange.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Campaign,
                            contentDescription = null,
                            tint = brandOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            notice,
                            fontSize = 12.sp,
                            color = brandOrange,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, tab ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTabSelected(index) }
                    ) {
                        Text(
                            tab,
                            fontSize = 15.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) brandOrange else TextGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .background(
                                    if (selectedTab == index) brandOrange else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("全部", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = brandOrange,
                    selectedLabelColor = Color.White
                )
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = brandOrange,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun MenuItemCard(
    food: FoodItem,
    cartQuantity: Int,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                if (food.image != null) {
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(food.image),
                        contentDescription = food.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(36.dp),
                        tint = Color.Gray
                    )
                }
                
                if (food.hasDiscount()) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart),
                        color = Color.Red,
                        shape = RoundedCornerShape(bottomEnd = 8.dp)
                    ) {
                        Text(
                            "优惠",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.name ?: "商品",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!food.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        food.description,
                        fontSize = 12.sp,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    "月售${food.salesVolume ?: 0}",
                    fontSize = 11.sp,
                    color = TextHint
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    Text(
                        "${food.price}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = brandOrange
                    )
                    if (food.hasDiscount() && food.originalPrice != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "¥${food.originalPrice}",
                            fontSize = 11.sp,
                            color = TextHint,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (cartQuantity > 0) {
                    Surface(
                        color = brandOrange.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "已加$cartQuantity",
                            fontSize = 11.sp,
                            color = brandOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Row {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            modifier = Modifier.size(20.dp),
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                    
                    FilledIconButton(
                        onClick = onAddToCart,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = brandOrange)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "加购", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsSection(reviews: List<Review>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("用户评价", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    "暂无评价",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            reviews.take(5).forEach { review ->
                ReviewCard(review = review)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.LightGray
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        review.userName ?: "匿名用户",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row {
                        repeat(5) { i ->
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i < (review.rating ?: 0)) brandOrange else Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                review.createTime?.let { time ->
                    Text(
                        time.take(10),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(review.content ?: "", fontSize = 13.sp, color = TextDark)
            
            // 显示商家回复
            val replyContent = review.replyContent ?: review.merchantReply
            if (!replyContent.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Store,
                                contentDescription = null,
                                tint = brandOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("商家回复", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = brandOrange)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(replyContent, fontSize = 13.sp, color = TextDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun MerchantInfoSection(
    merchant: Merchant?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("商家信息", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                merchant?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    InfoRow(Icons.Filled.Info, "简介", desc)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                merchant?.notice?.takeIf { it.isNotBlank() }?.let { notice ->
                    InfoRow(Icons.Filled.Campaign, "公告", notice)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                InfoRow(Icons.Filled.LocationOn, "地址", merchant?.address ?: "上海市静安区南京西路")
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(Icons.Filled.Schedule, "营业时间", merchant?.businessHours ?: "10:00 - 22:00")
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(Icons.Filled.Phone, "电话", merchant?.phone ?: "400-123-4567")
                
                merchant?.packFee?.let { packFee ->
                    if (packFee > BigDecimal.ZERO) {
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(Icons.Filled.Inventory, "包装费", "¥${packFee}")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = brandOrange, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextGray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FloatingCartBar(
    totalAmount: BigDecimal,
    quantity: Int,
    deliveryFee: BigDecimal,
    minAmount: BigDecimal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMinMet = totalAmount >= minAmount
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF2D2D2D)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = "购物车",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                if (quantity > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp),
                        containerColor = brandOrange
                    ) {
                        Text(if (quantity > 99) "99+" else quantity.toString(), fontSize = 10.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        String.format("%.2f", totalAmount),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                if (!isMinMet) {
                    Text(
                        "还差¥${minAmount.subtract(totalAmount)}起送",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMinMet) brandOrange else Color.Gray
                ),
                enabled = isMinMet
            ) {
                Text(if (isMinMet) "去结算" else "差¥${minAmount.subtract(totalAmount)}", fontSize = 14.sp)
            }
        }
    }
}
