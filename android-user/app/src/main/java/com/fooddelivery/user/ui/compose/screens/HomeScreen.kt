package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.Merchant
import com.fooddelivery.user.model.MerchantCategory
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.ui.compose.components.LoadingIndicator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class Category(
    val id: Long,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun getCategoryIcon(categoryName: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (categoryName) {
        "美食", "中餐" -> Icons.Filled.LunchDining
        "甜点", "甜品" -> Icons.Filled.Icecream
        "饮品" -> Icons.Filled.LocalCafe
        "超市便利" -> Icons.Filled.Storefront
        "生鲜" -> Icons.Filled.LocalMall
        "西餐" -> Icons.Filled.Restaurant
        "快餐" -> Icons.Filled.Fastfood
        "日韩料理", "日料", "韩餐" -> Icons.Filled.RamenDining
        "烧烤" -> Icons.Filled.OutdoorGrill
        else -> Icons.Filled.Restaurant
    }
}

@Composable
fun HomeScreen(
    merchants: List<Merchant> = emptyList(),
    recommendedMerchants: List<Merchant> = emptyList(),
    merchantCategories: List<MerchantCategory> = emptyList(),
    currentAddress: String = "上海市 浦东新区",
    isLoading: Boolean = false,
    onNavigateToMerchant: (Long) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToAddressSelect: () -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
    onActivityClick: (String) -> Unit = {},
    cartItemCount: Int = 0
) {
    var selectedCategory by remember { mutableStateOf<Long?>(null) }
    
    val defaultCategories = remember {
        listOf(
            Category(1, "美食", Icons.Filled.LunchDining),
            Category(2, "甜点", Icons.Filled.Icecream),
            Category(3, "饮品", Icons.Filled.LocalCafe),
            Category(4, "超市便利", Icons.Filled.Storefront),
            Category(5, "生鲜", Icons.Filled.LocalMall)
        )
    }

    val categories = remember(merchantCategories) {
        if (merchantCategories.isNotEmpty()) {
            merchantCategories.mapIndexed { index, cat ->
                Category(
                    id = cat.id ?: (index + 1).toLong(),
                    name = cat.categoryName ?: "",
                    icon = getCategoryIcon(cat.categoryName)
                )
            }
        } else {
            defaultCategories
        }
    }

    val filteredMerchants = remember(merchants, selectedCategory) {
        if (selectedCategory == null) {
            merchants
        } else {
            merchants.filter { merchant ->
                merchant.categoryId != null && merchant.categoryId == selectedCategory
            }
        }
    }

    val displayTitle = remember(selectedCategory, categories) {
        if (selectedCategory == null) {
            "为您推荐"
        } else {
            categories.find { it.id == selectedCategory }?.name ?: "商家列表"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (-60).dp)
                .size(300.dp)
                .background(brandOrange.copy(alpha = 0.1f), RoundedCornerShape(50))
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 100.dp)
                .size(200.dp)
                .background(brandOrange.copy(alpha = 0.05f), RoundedCornerShape(50))
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                GlassHeader(
                    currentAddress = currentAddress,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToAddressSelect = onNavigateToAddressSelect
                )
            }

            item {
                CategoryGrid(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategoryClick = { category ->
                        selectedCategory = if (selectedCategory == category.id) null else category.id
                    }
                )
            }

            item {
                BannerSection(onActivityClick = onActivityClick)
            }

            item {
                Text(
                    displayTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(color = brandOrange)
                    }
                }
            } else if (filteredMerchants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("暂无商家数据", color = TextGray)
                        }
                    }
                }
            } else {
                items(filteredMerchants) { merchant ->
                    MerchantCard(
                        merchant = merchant,
                        onClick = { onNavigateToMerchant(merchant.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassHeader(
    currentAddress: String,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAddressSelect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.6f),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onNavigateToAddressSelect)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = brandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        currentAddress,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .padding(start = 4.dp)
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "通知",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable(onClick = onNavigateToSearch),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f)
                    )
                    Text(
                        "想吃什么？搜索美食",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    Button(
                        onClick = onNavigateToSearch,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("搜索", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    selectedCategory: Long?,
    onCategoryClick: (Category) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        categories.forEach { category ->
            CategoryItem(
                category = category,
                isSelected = selectedCategory == category.id,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) brandOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    category.icon,
                    contentDescription = category.name,
                    tint = brandOrange,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            category.name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) brandOrange else Color.Unspecified
        )
    }
}

@Composable
private fun BannerSection(onActivityClick: (String) -> Unit = {}) {
    val banners = remember {
        listOf(
            Banner("外卖节盛典", "限时半价", "精选套餐5折起", "activity_1", false),
            Banner("新品上市", "满30减15", "周末专享尝鲜价", "activity_2", true),
            Banner("会员专享", "免费配送", "新用户首月免运费", "activity_3", false)
        )
    }

    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(banners) { banner ->
            BannerCard(
                banner = banner,
                onClick = { onActivityClick(banner.id) }
            )
        }
    }
}

data class Banner(
    val tag: String,
    val title: String,
    val subtitle: String,
    val id: String,
    val isEnded: Boolean = false
)

@Composable
private fun BannerCard(banner: Banner, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .width(320.dp)
            .height(140.dp)
            .clickable(enabled = !banner.isEnded, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = brandOrange.copy(alpha = 0.2f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
            
            if (banner.isEnded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "活动已结束",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(20.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        banner.tag,
                        color = brandOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    banner.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    banner.subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MerchantCard(
    merchant: Merchant,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageUtils.getFullImageUrl(merchant.logo)
                        .ifBlank { ImageUtils.getFullImageUrl(merchant.banner) },
                    contentDescription = merchant.name,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        merchant.name ?: "未知商家",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF1D1512)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "${merchant.rating ?: 4.8}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF5A5A5A)
                            )
                        }
                        
                        Text(
                            "月售 ${merchant.salesVolume ?: 0}+",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFA8C16).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "起送 ¥${merchant.minAmount?.toDouble()?.toInt() ?: 0}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFA8C16)
                                    )
                                }
                            }
                            if ((merchant.deliveryFee ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                                Text(
                                    "配送 ¥${merchant.deliveryFee?.toDouble()?.toInt() ?: 0}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9E9E9E)
                                )
                            } else {
                                Text(
                                    "免配送",
                                    fontSize = 11.sp,
                                    color = Color(0xFF52C41A)
                                )
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "${merchant.averageDeliveryTime}分钟",
                                fontSize = 12.sp,
                                color = Color(0xFF5A5A5A)
                            )
                        }
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = Color(0xFFF0F0F0),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocalOffer,
                    contentDescription = null,
                    tint = Color(0xFFFA8C16),
                    modifier = Modifier.size(14.dp)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CouponTag(text = "满40减10")
                    CouponTag(text = "首单立减")
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CouponTag(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFFF2E8)
    ) {
        Text(
            text,
            fontSize = 10.sp,
            color = Color(0xFFFA8C16),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
