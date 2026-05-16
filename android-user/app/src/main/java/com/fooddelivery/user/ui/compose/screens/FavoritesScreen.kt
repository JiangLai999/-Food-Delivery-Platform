package com.fooddelivery.user.ui.compose.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.utils.ImageUtils

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)
private val textPrimary = Color(0xFF1D150C)
private val textSecondary = Color(0xFFA17745)

data class FavoriteItem(
    val id: Long, // Favorite record ID
    val merchantId: Long? = null, // Merchant ID (for navigation)
    val name: String,
    val image: String?,
    val type: FavoriteType,
    val merchantName: String? = null,
    val rating: Float? = null,
    val salesVolume: Int? = null,
    val price: String? = null,
    val deliveryFee: Double? = null,
    val deliveryTime: String? = null
)

enum class FavoriteType {
    MERCHANT, DISH
}

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteItem> = emptyList(),
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onItemClick: (FavoriteItem) -> Unit = {},
    onRemoveFavorite: (Long) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("餐厅", "菜品")
    
    var showDeleteMode by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    val displayFavorites = remember(favorites, selectedTab) {
        when (selectedTab) {
            1 -> favorites.filter { it.type == FavoriteType.DISH }
            else -> favorites.filter { it.type == FavoriteType.MERCHANT }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().background(backgroundLight)
    ) {
        // Header with glass effect
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = backgroundLight.copy(alpha = 0.8f),
            tonalElevation = 0.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.5f)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回",
                                    tint = textPrimary
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "我的收藏",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = { showDeleteMode = !showDeleteMode },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.5f)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    if (showDeleteMode) Icons.Filled.Done else Icons.Outlined.Edit,
                                    contentDescription = if (showDeleteMode) "完成" else "编辑",
                                    tint = textPrimary
                                )
                            }
                        }
                    }
                }
                
                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) 
                                        Color.White 
                                    else 
                                        textPrimary.copy(alpha = 0.05f)
                                )
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) brandOrange else textPrimary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                Divider(color = textPrimary.copy(alpha = 0.05f))
            }
        }
        
        // Show loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = brandOrange)
            }
        } else if (displayFavorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = textPrimary.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = "暂无收藏",
                        fontSize = 16.sp,
                        color = textPrimary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "去首页逛逛吧",
                        fontSize = 14.sp,
                        color = textPrimary.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayFavorites, key = { it.id }) { favorite ->
                    if (favorite.type == FavoriteType.MERCHANT) {
                        MerchantFavoriteCard(
                            favorite = favorite,
                            showDeleteMode = showDeleteMode,
                            onClick = { onItemClick(favorite) },
                            onDelete = { onRemoveFavorite(favorite.id) }
                        )
                    } else {
                        DishFavoriteCard(
                            favorite = favorite,
                            showDeleteMode = showDeleteMode,
                            onClick = { onItemClick(favorite) },
                            onDelete = { onRemoveFavorite(favorite.id) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun MerchantFavoriteCard(
    favorite: FavoriteItem,
    showDeleteMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                enabled = !showDeleteMode,
                onClick = {
                    isPressed = true
                    onClick()
                    isPressed = false
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Column {
                // Banner Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF3F4F6))
                ) {
                    if (favorite.image != null) {
                        AsyncImage(
                            model = ImageUtils.getFullImageUrl(favorite.image),
                            contentDescription = favorite.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Filled.Store,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center).size(48.dp),
                            tint = Color.Gray
                        )
                    }
                    
                    // Favorite icon
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showDeleteMode) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "删除",
                                tint = brandOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "收藏",
                                tint = brandOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Delivery time badge
                    if (favorite.deliveryTime != null) {
                        Surface(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.BottomStart)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color.White.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = brandOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = favorite.deliveryTime!!,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
                
                // Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = favorite.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${favorite.rating ?: 5.0}",
                                    fontSize = 12.sp,
                                    color = textPrimary.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "•",
                                    color = textPrimary.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = favorite.salesVolume?.let { "月售${it}+" } ?: "好评率高",
                                    fontSize = 12.sp,
                                    color = textPrimary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = textPrimary.copy(alpha = 0.05f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "配送费 ${favorite.deliveryFee?.let { "¥${it}" } ?: "免运费"}",
                            fontSize = 12.sp,
                            color = textPrimary.copy(alpha = 0.7f)
                        )
                        
                        if (showDeleteMode) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "删除",
                                    tint = brandOrange
                                )
                            }
                        } else {
                            Button(
                                onClick = onClick,
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "进店逛逛",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DishFavoriteCard(
    favorite: FavoriteItem,
    showDeleteMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !showDeleteMode,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dish Image
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                if (favorite.image != null) {
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(favorite.image),
                        contentDescription = favorite.name,
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
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = favorite.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (favorite.merchantName != null) {
                    Text(
                        text = "来自: ${favorite.merchantName}",
                        fontSize = 11.sp,
                        color = textPrimary.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = favorite.salesVolume?.let { "月售 ${it}+" } ?: "好评度高",
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.heightIn(min = 90.dp)
            ) {
                Text(
                    text = favorite.price ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandOrange
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (showDeleteMode) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = brandOrange
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(brandOrange)
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = "添加",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
