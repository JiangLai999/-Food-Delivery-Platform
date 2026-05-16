package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.Merchant
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.ui.compose.components.LoadingIndicator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class SearchCategory(
    val id: Long,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun SearchScreen(
    searchResults: List<Merchant> = emptyList(),
    searchFoodResults: List<com.fooddelivery.user.model.FoodItem> = emptyList(),
    currentAddress: String = "静安区南京西路",
    isLoading: Boolean = false,
    onBackClick: () -> Unit = {},
    onSearch: (String, Long?, String?) -> Unit = { _, _, _ -> },
    onMerchantClick: (Long) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }
    
    val sortOptions = listOf(
        Pair("default", "综合排序"),
        Pair("rating_desc", "评分最高"),
        Pair("distance_asc", "距离最近"),
        Pair("price_asc", "价格从低到高"),
        Pair("price_desc", "价格从高到低"),
        Pair("sales_desc", "销量最高")
    )
    
    val selectedSort by remember { mutableStateOf(sortOptions[0].first) }
    
    val categories = remember {
        listOf(
            SearchCategory(0, "全部", Icons.Filled.Restaurant),
            SearchCategory(1, "美食", Icons.Filled.LunchDining),
            SearchCategory(2, "甜点", Icons.Filled.Icecream),
            SearchCategory(3, "饮品", Icons.Filled.LocalCafe),
            SearchCategory(4, "超市便利", Icons.Filled.Storefront),
            SearchCategory(5, "生鲜", Icons.Filled.LocalMall)
        )
    }
    
    val sampleResults = remember {
        listOf(
            SearchMerchant(
                id = 1,
                name = "轻食主义 · 健康餐 (静安店)",
                rating = 4.8f,
                salesVolume = 1200,
                deliveryTime = 30,
                distance = "2.1km",
                minAmount = BigDecimal("20"),
                deliveryFee = BigDecimal("3"),
                tags = listOf("招牌：牛油果鸡肉沙拉", "全麦意面"),
                discount = "首单减¥15"
            ),
            SearchMerchant(
                id = 2,
                name = "锦江川菜馆 · 地道川味",
                rating = 4.6f,
                salesVolume = 800,
                deliveryTime = 45,
                distance = "3.5km",
                minAmount = BigDecimal("50"),
                deliveryFee = BigDecimal.ZERO,
                tags = listOf("招牌：麻婆豆腐", "回锅肉"),
                discount = null
            ),
            SearchMerchant(
                id = 3,
                name = "Monster Burger 巨兽汉堡",
                rating = 4.9f,
                salesVolume = 2500,
                deliveryTime = 25,
                distance = "1.2km",
                minAmount = BigDecimal("30"),
                deliveryFee = BigDecimal("5"),
                tags = listOf("招牌：经典芝士培根堡", "粗薯条"),
                discount = "限时8折"
            )
        )
    }
    
    val displayResults: List<SearchMerchant> = if (searchResults.isEmpty()) sampleResults else searchResults.map { m ->
        SearchMerchant(
            id = m.id ?: 0L,
            name = m.name ?: "",
            rating = m.rating ?: 4.5f,
            salesVolume = m.salesVolume ?: 0,
            deliveryTime = m.averageDeliveryTime,
            distance = "${m.distance}km",
            minAmount = m.minAmount ?: BigDecimal.ZERO,
            deliveryFee = m.deliveryFee ?: BigDecimal.ZERO,
            tags = emptyList(),
            discount = null
        )
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
            GlassSearchHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearch = { onSearch(searchQuery, if (selectedCategory == 0L) null else selectedCategory, sortOptions.getOrNull(selectedFilter)?.first) },
                currentAddress = currentAddress
            )

            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            FilterRow(
                filters = sortOptions.map { it.second },
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (searchFoodResults.isNotEmpty()) {
                        item {
                            Text(
                                "食品",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(searchFoodResults) { food ->
                            SearchFoodCard(
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
                            if (selectedCategory != null && selectedCategory != 0L)
                                "${categories.find { it.id == selectedCategory }?.name ?: ""}商家"
                            else "推荐餐厅",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(displayResults) { merchant ->
                        SearchMerchantCard(
                            merchant = merchant,
                            onClick = { onMerchantClick(merchant.id) }
                        )
                    }

                    if (displayResults.isEmpty() && searchFoodResults.isEmpty()) {
                        item {
                            EmptyStateCard()
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

data class SearchMerchant(
    val id: Long,
    val name: String,
    val rating: Float,
    val salesVolume: Int,
    val deliveryTime: Int,
    val distance: String,
    val minAmount: BigDecimal,
    val deliveryFee: BigDecimal,
    val tags: List<String>,
    val discount: String?
)

@Composable
private fun GlassSearchHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    currentAddress: String
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
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = brandOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("我的地址", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { onSearch() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = brandOrange.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("搜索美食、店铺、食材...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() })
                    )
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Filled.Tune, contentDescription = "筛选", tint = brandOrange)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<SearchCategory>,
    selectedCategory: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { 
                    onCategorySelected(if (category.id == 0L) null else category.id) 
                }
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedCategory == category.id || (selectedCategory == null && category.id == 0L)) 
                        brandOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.6f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            category.icon,
                            contentDescription = category.name,
                            tint = if (selectedCategory == category.id || (selectedCategory == null && category.id == 0L)) 
                                brandOrange else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    category.name,
                    fontSize = 11.sp,
                    color = if (selectedCategory == category.id || (selectedCategory == null && category.id == 0L)) 
                        brandOrange else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    filters: List<String>,
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters.size) { index ->
            FilterChip(
                selected = selectedFilter == index,
                onClick = { onFilterSelected(index) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(filters[index], fontSize = 14.sp)
                        if (index in listOf(0, 3)) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = brandOrange,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.height(36.dp)
            )
        }
    }
}

@Composable
private fun SearchMerchantCard(
    merchant: SearchMerchant,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.6f)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = null,
                    contentDescription = merchant.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                if (merchant.discount != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            merchant.discount,
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        merchant.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.MoreHoriz, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f))
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = brandOrange, modifier = Modifier.size(14.dp))
                    Text(" ${merchant.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    Text(" | 月售 ${merchant.salesVolume}+", fontSize = 12.sp, color = Color.Gray)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(" ${merchant.deliveryTime}分钟", fontSize = 11.sp, color = Color.Gray)
                        Text("  ${merchant.distance}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        if (merchant.deliveryFee > BigDecimal.ZERO) "起送 ¥${merchant.minAmount.toInt()}" else "免配送费",
                        fontSize = 11.sp,
                        color = brandOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    merchant.tags.forEach { tag ->
                        Surface(
                            color = brandOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                tag,
                                fontSize = 10.sp,
                                color = brandOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFoodCard(
    food: com.fooddelivery.user.model.FoodItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.6f)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = ImageUtils.getFullImageUrl(food.image),
                    contentDescription = food.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.name ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                                "¥${origPrice}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = brandOrange.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("没有更多餐厅了", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { }) {
                Text("查看更多区域", color = brandOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}
