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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.Merchant
import com.fooddelivery.user.ui.compose.components.LoadingIndicator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class SearchResultMerchant(
    val id: Long,
    val name: String,
    val rating: Float,
    val salesVolume: Int,
    val deliveryTime: Int,
    val distance: String,
    val minAmount: BigDecimal,
    val deliveryFee: BigDecimal,
    val tags: List<String>,
    val discount: String?,
    val imageUrl: String?
)

@Composable
fun SearchResultScreen(
    searchResults: List<Merchant> = emptyList(),
    searchFoodResults: List<com.fooddelivery.user.model.FoodItem> = emptyList(),
    currentAddress: String = "我的地址",
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearch: (String, Long?, String?) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {},
    onMerchantClick: (Long) -> Unit = {},
    isLoading: Boolean = false
) {
    var selectedCategory by remember { mutableStateOf<Long?>(null) }
    var selectedFilter by remember { mutableStateOf(0) }
    
    val sortOptions = listOf(
        Pair("default", "综合排序"),
        Pair("rating_desc", "评分最高"),
        Pair("distance_asc", "距离最近"),
        Pair("price_asc", "价格从低到高"),
        Pair("price_desc", "价格从高到低"),
        Pair("sales_desc", "销量最高")
    )
    
    val categories = listOf(
        SearchCategory(0, "全部", Icons.Filled.Restaurant),
        SearchCategory(1, "美食", Icons.Filled.LunchDining),
        SearchCategory(2, "甜点", Icons.Filled.Icecream),
        SearchCategory(3, "饮品", Icons.Filled.LocalCafe),
        SearchCategory(4, "超市便利", Icons.Filled.Storefront),
        SearchCategory(5, "生鲜", Icons.Filled.LocalMall)
    )
    
    val displayResults: List<SearchResultMerchant> = remember(searchResults) {
        searchResults.map { m ->
            SearchResultMerchant(
                id = m.id ?: 0L,
                name = m.name ?: "",
                rating = m.rating ?: 4.5f,
                salesVolume = m.salesVolume ?: 0,
                deliveryTime = m.averageDeliveryTime ?: 30,
                distance = "${m.distance ?: 1.0}km",
                minAmount = m.minAmount ?: BigDecimal(20),
                deliveryFee = m.deliveryFee ?: BigDecimal(3),
                tags = m.tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
discount = null,
        imageUrl = ImageUtils.getFullImageUrl(m.logo)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(brandOrange.copy(alpha = 0.1f), backgroundLight),
                    radius = 800f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchResultHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearch = { onSearch(searchQuery, if (selectedCategory == 0L) null else selectedCategory, sortOptions.getOrNull(selectedFilter)?.first) },
                currentAddress = currentAddress,
                onBackClick = onBackClick
            )

            FilterChipsRow(
                sortOptions = sortOptions,
                selectedFilter = selectedFilter,
                onFilterSelected = { 
                    selectedFilter = it
                    onSearch(searchQuery, if (selectedCategory == 0L) null else selectedCategory, sortOptions.getOrNull(it)?.first)
                }
            )

            CategoryChipsRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { 
                    selectedCategory = it
                    onSearch(searchQuery, if (it == 0L) null else it, sortOptions.getOrNull(selectedFilter)?.first)
                }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(color = brandOrange)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (searchFoodResults.isNotEmpty()) {
                        item {
                            Text(
                                "食品",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(searchFoodResults) { food ->
                            FoodResultCard(
                                food = food,
                                onClick = { onMerchantClick(food.merchantId ?: 0L) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    item {
                        Text(
                            "商家",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (displayResults.isEmpty() && searchFoodResults.isEmpty()) {
                        item {
                            EmptyState()
                        }
                    } else {
                        items(displayResults) { merchant ->
                            MerchantResultCard(
                                merchant = merchant,
                                onClick = { onMerchantClick(merchant.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    currentAddress: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onBackClick)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = brandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        currentAddress,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("搜索美食、店铺、食材...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Button(
                        onClick = onSearch,
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
private fun FilterChipsRow(
    sortOptions: List<Pair<String, String>>,
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortOptions.size) { index ->
            val isSelected = selectedFilter == index
            Surface(
                modifier = Modifier
                    .height(36.dp)
                    .clickable { onFilterSelected(index) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) brandOrange else Color.White.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sortOptions[index].second,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF333333)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    categories: List<SearchCategory>,
    selectedCategory: Long?,
    onCategorySelected: (Long) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val isSelected = selectedCategory == category.id || (selectedCategory == null && category.id == 0L)
            Surface(
                modifier = Modifier
                    .height(32.dp)
                    .clickable { onCategorySelected(category.id) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) brandOrange.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = if (isSelected) brandOrange else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        category.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) brandOrange else Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun MerchantResultCard(
    merchant: SearchResultMerchant,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageUtils.getFullImageUrl(merchant.imageUrl),
                contentDescription = merchant.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        merchant.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = brandOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "${merchant.rating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandOrange,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        " | 月售 ${merchant.salesVolume}+",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "${merchant.deliveryTime}分钟",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        " | ${merchant.distance}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        if (merchant.deliveryFee.toInt() == 0) "免配送费" else "起送¥${merchant.minAmount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = brandOrange
                    )
                }

                if (merchant.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        merchant.tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = brandOrange.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    tag,
                                    fontSize = 10.sp,
                                    color = brandOrange,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
private fun FoodResultCard(
    food: com.fooddelivery.user.model.FoodItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageUtils.getFullImageUrl(food.image),
                contentDescription = food.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    food.name ?: "",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    food.description ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "¥${food.price}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandOrange
                    )
                    
                    food.originalPrice?.let { origPrice ->
                        if (origPrice > (food.price ?: BigDecimal.ZERO)) {
                            Text(
                                " ¥${origPrice}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Restaurant,
                contentDescription = null,
                tint = brandOrange.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "没有找到相关商家",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
