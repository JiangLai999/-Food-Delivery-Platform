package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.CartItem
import com.fooddelivery.user.model.FoodItem
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.ui.compose.components.LoadingIndicator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)

@Composable
fun CartScreen(
    cartItems: List<CartItem> = emptyList(),
    merchantName: String = "",
    deliveryFee: BigDecimal = BigDecimal.ZERO,
    minOrderAmount: BigDecimal = BigDecimal.ZERO,
    isLoading: Boolean = false,
    onQuantityChange: (Long, Int) -> Unit = { _, _ -> },
    onClearCart: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    onContinueShopping: () -> Unit = {},
    onBack: () -> Unit = {},
    recommendedItems: List<FoodItem> = emptyList(),
    onAddRecommended: (FoodItem) -> Unit = {}
) {
    val totalAmount = remember(cartItems) {
        cartItems.fold(BigDecimal.ZERO) { total, item ->
            total.add(item.getSubtotal())
        }
    }
    
    val finalAmount = totalAmount.add(deliveryFee)
    val isMinOrderMet = totalAmount >= minOrderAmount
    val totalQuantity = cartItems.sumOf { it.quantity }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    text = "购物车",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("清空", color = TextGray, fontSize = 13.sp)
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            EmptyCartState(onContinueShopping = onContinueShopping)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (merchantName.isNotEmpty()) {
                    item {
                        MerchantInfoCard(
                            merchantName = merchantName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                items(cartItems, key = { it.foodId }) { item ->
                    CartItemCard(
                        item = item,
                        onQuantityChange = { newQty ->
                            onQuantityChange(item.foodId, newQty)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                if (recommendedItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        CrossSellSection(
                            items = recommendedItems,
                            onAddClick = onAddRecommended,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            if (!isMinOrderMet && minOrderAmount > BigDecimal.ZERO) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WarningYellow.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "还差¥${minOrderAmount.subtract(totalAmount)}满足起送价",
                            fontSize = 12.sp,
                            color = WarningYellow
                        )
                    }
                }
            }

            CartBottomBar(
                totalAmount = totalAmount,
                deliveryFee = deliveryFee,
                finalAmount = finalAmount,
                totalQuantity = totalQuantity,
                isMinOrderMet = isMinOrderMet,
                isLoading = isLoading,
                onCheckout = onNavigateToCheckout
            )
        }
    }
}

@Composable
private fun EmptyCartState(onContinueShopping: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(60.dp),
                color = brandOrange.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = brandOrange.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("购物车空空如也", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text("快去挑选美食吧~", color = TextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onContinueShopping,
                colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(160.dp).height(48.dp)
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("去逛逛", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MerchantInfoCard(
    merchantName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = brandOrange.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Store, contentDescription = null, tint = brandOrange, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(merchantName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("商家自营", fontSize = 12.sp, color = TextGray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .size(85.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                if (item.foodImage != null) {
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(item.foodImage),
                        contentDescription = item.foodName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.foodName ?: "未知商品",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "¥${item.price}",
                    color = brandOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = brandOrange.copy(alpha = 0.1f),
                            contentColor = brandOrange
                        )
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "减少", modifier = Modifier.size(16.dp))
                    }
                    
                    Text(
                        item.quantity.toString(),
                        modifier = Modifier.widthIn(min = 32.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    FilledIconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = brandOrange)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "增加", modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "小计 ¥${item.getSubtotal()}",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun CrossSellSection(
    items: List<FoodItem>,
    onAddClick: (FoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AddShoppingCart, contentDescription = null, tint = brandOrange, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("为你推荐", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                CrossSellItem(
                    item = item,
                    onAddClick = { onAddClick(item) }
                )
            }
        }
    }
}

@Composable
private fun CrossSellItem(
    item: FoodItem,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color.LightGray)
            ) {
                if (item.image != null) {
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(item.image),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    item.name ?: "商品",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("¥", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                        Text("${item.price}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    }
                    
                    FilledIconButton(
                        onClick = onAddClick,
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = brandOrange)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "加购", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CartBottomBar(
    totalAmount: BigDecimal,
    deliveryFee: BigDecimal,
    finalAmount: BigDecimal,
    totalQuantity: Int,
    isMinOrderMet: Boolean,
    isLoading: Boolean,
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(16.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("商品小计", color = TextGray, fontSize = 13.sp)
                Text("¥${totalAmount}", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("配送费", color = TextGray, fontSize = 13.sp)
                Text(
                    if (deliveryFee > BigDecimal.ZERO) "¥${deliveryFee}" else "免配送费",
                    fontSize = 13.sp,
                    color = if (deliveryFee > BigDecimal.ZERO) TextDark else SuccessGreen
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("¥", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                        Text(
                            String.format("%.2f", finalAmount),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = brandOrange
                        )
                    }
                }
                
                Button(
                    onClick = onCheckout,
                    modifier = Modifier.width(140.dp).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMinOrderMet) brandOrange else Color.Gray
                    ),
                    enabled = isMinOrderMet && !isLoading
                ) {
                    if (isLoading) {
                        Text("...", color = Color.White, fontSize = 18.sp)
                    } else {
                        Text("去结算($totalQuantity)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
