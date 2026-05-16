package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.FoodItem
import com.fooddelivery.user.model.Merchant
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.utils.FoodSpecGenerator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)

data class FoodSpec(
    val id: Long,
    val name: String,
    val price: BigDecimal
)

data class SpecGroup(
    val id: Long,
    val name: String,
    val required: Boolean,
    val options: List<FoodSpec>
)

@Composable
fun ProductDetailScreen(
    food: FoodItem? = null,
    merchant: Merchant? = null,
    currentQuantity: Int = 0,
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onAddToCart: (Int) -> Unit = {},
    onQuantityChange: (Int) -> Unit = {}
) {
    var selectedQuantity by remember { mutableStateOf(1) }
    var showSpecSheet by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    
    // 使用智能规格生成器
    val displayFood = food ?: FoodItem().apply {
        id = 1L
        name = "经典牛肉汉堡"
        description = "精选澳洲安格斯牛肉，搭配新鲜蔬菜和特制酱料，口感鲜嫩多汁"
        price = BigDecimal("45")
        originalPrice = BigDecimal("58")
        salesVolume = 2000
    }
    
    // 根据餐品类型生成描述
    val foodDescription = remember(displayFood) {
        FoodSpecGenerator.generateFoodDescription(displayFood)
    }
    
    // 根据餐品类型动态生成规格组
    val specGroups = remember(displayFood) {
        FoodSpecGenerator.generateSpecGroups(displayFood).map { group ->
            SpecGroup(
                id = group.id,
                name = group.name,
                required = group.required,
                options = group.options.map { opt ->
                    FoodSpec(opt.id, opt.name, opt.price)
                }
            )
        }
    }
    
    val selectedSpecs = remember { mutableStateMapOf<Long, FoodSpec>() }
    
    // 当餐品变化时，重置规格选择
    LaunchedEffect(displayFood.id) {
        selectedSpecs.clear()
        specGroups.forEach { group ->
            if (group.required && group.options.isNotEmpty()) {
                selectedSpecs[group.id] = group.options.first()
            }
        }
    }
    
    val displayMerchant = merchant ?: Merchant().apply {
        id = 1L
        name = "Shake Shack 汉堡"
        rating = 4.9f
        salesVolume = 2000
        deliveryFee = BigDecimal("5")
        minAmount = BigDecimal("20")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            AsyncImage(
                model = ImageUtils.getFullImageUrl(displayFood.image),
                contentDescription = displayFood.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    ))
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "分享", tint = Color.White)
                    }
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
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
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    color = brandOrange,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        displayFood.categoryName ?: "招牌推荐",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    displayFood.name ?: "商品",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("¥", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                                Text(
                                    "${displayFood.price}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = brandOrange
                                )
                                if (displayFood.hasDiscount() && displayFood.originalPrice != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "¥${displayFood.originalPrice}",
                                        fontSize = 14.sp,
                                        color = TextHint,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                            
                            if (displayFood.hasDiscount()) {
                                Surface(
                                    color = Color.Red.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "限时${((1 - displayFood.price.divide(displayFood.originalPrice!!, 2, BigDecimal.ROUND_HALF_UP).toDouble()) * 100).toInt()}%OFF",
                                        color = Color.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = brandOrange, modifier = Modifier.size(16.dp))
                            Text(" ${displayMerchant.rating ?: 4.8}", fontWeight = FontWeight.Bold, color = brandOrange, fontSize = 13.sp)
                            Text(" | 月售${displayFood.salesVolume ?: 0}+", color = TextGray, fontSize = 13.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 使用智能生成的描述
                        Text(
                            foodDescription.longDesc,
                            fontSize = 13.sp,
                            color = TextGray,
                            lineHeight = 20.sp,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // 显示亮点标签
                        if (foodDescription.highlight.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(foodDescription.highlight) { tag ->
                                    Surface(
                                        color = brandOrange.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "✓ $tag",
                                            fontSize = 11.sp,
                                            color = brandOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SpecSelectionCard(
                    specGroups = specGroups,
                    selectedSpecs = selectedSpecs,
                    onSpecClick = { groupId, spec ->
                        selectedSpecs[groupId] = spec
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("配送信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = brandOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("约25-35分钟送达", fontSize = 13.sp, color = TextDark)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = brandOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (displayMerchant.deliveryFee?.compareTo(BigDecimal.ZERO) == 0) "免配送费" else "配送费 ¥${displayMerchant.deliveryFee}",
                                fontSize = 13.sp,
                                color = TextDark
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.AttachMoney, contentDescription = null, tint = brandOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("满 ¥${displayMerchant.minAmount ?: 0} 起送", fontSize = 13.sp, color = TextDark)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Store, contentDescription = "店铺", tint = TextGray)
                    }
                    Text("店铺", fontSize = 10.sp, color = TextGray)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledIconButton(
                        onClick = { if (selectedQuantity > 1) selectedQuantity-- },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = brandOrange.copy(alpha = 0.1f),
                            contentColor = brandOrange
                        )
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "减少", modifier = Modifier.size(18.dp))
                    }
                    
                    Text(
                        selectedQuantity.toString(),
                        modifier = Modifier.widthIn(min = 32.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    FilledIconButton(
                        onClick = { selectedQuantity++ },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = brandOrange)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "增加", modifier = Modifier.size(18.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = { onAddToCart(selectedQuantity) },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("加入购物车", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecSelectionCard(
    specGroups: List<SpecGroup>,
    selectedSpecs: Map<Long, FoodSpec>,
    onSpecClick: (Long, FoodSpec) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("规格选择", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("必选", fontSize = 11.sp, color = Color.Red, modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            specGroups.forEach { group ->
                Text(
                    "${group.name}${if (group.required) "" else "(可选)"}",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.options.forEach { spec ->
                        val isSelected = selectedSpecs[group.id] == spec
                        SpecChip(
                            spec = spec,
                            isSelected = isSelected,
                            onClick = { onSpecClick(group.id, spec) }
                        )
                    }
                }
                
                if (group != specGroups.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SpecChip(
    spec: FoodSpec,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) brandOrange.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, brandOrange) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                spec.name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) brandOrange else TextDark
            )
            if (spec.price > BigDecimal.ZERO) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "+¥${spec.price}",
                    fontSize = 11.sp,
                    color = if (isSelected) brandOrange else TextGray
                )
            }
        }
    }
}
