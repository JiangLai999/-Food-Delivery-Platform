package com.fooddelivery.user.ui.compose.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.Order
import com.fooddelivery.user.model.OrderItem
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.ui.compose.components.LoadingIndicator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

@Composable
fun OrderListScreen(
    orders: List<Order> = emptyList(),
    isLoading: Boolean = false,
    initialFilter: String? = null, // Filter: "pending", "delivering", "unreviewed", "refund"
    onBackClick: () -> Unit = {},
    onTrackOrder: (Long) -> Unit = {},
    onReorder: (Long) -> Unit = {},
    onOrderDetail: (Long) -> Unit = {},
    onCancelOrder: (Long) -> Unit = {},
    onConfirmOrder: (Long) -> Unit = {},
    onReviewClick: (Long, Long, Boolean) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Determine initial tab based on filter
    LaunchedEffect(initialFilter) {
        when (initialFilter) {
            "pending" -> selectedTab = 0 // 待接单 -> 进行中
            "delivering" -> selectedTab = 0 // 配送中 -> 进行中
            "unreviewed" -> selectedTab = 1 // 待评价 -> 历史
            "refund" -> selectedTab = 1 // 退款售后 -> 历史
            else -> {}
        }
    }
    
    val tabs = listOf("进行中", "历史订单")
    
    val ongoingOrders = remember(orders, initialFilter) {
        when (initialFilter) {
            "pending" -> orders.filter { it.status == 1 } // 待接单
            "delivering" -> orders.filter { it.status == 3 } // 配送中
            else -> orders.filter { it.status in listOf(0, 1, 2, 3) }
        }
    }
    
    val historyOrders = remember(orders, initialFilter) {
        when (initialFilter) {
            "unreviewed" -> orders.filter { it.status == 4 } // 已完成(待评价)
            "refund" -> orders.filter { it.status == 5 } // 已取消(退款售后)
            else -> orders.filter { it.status == 4 || it.status == 5 }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        GlassHeader(onBackClick = onBackClick)
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = brandOrange
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) brandOrange else Color.Gray
                        )
                    }
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(color = brandOrange)
            }
        } else {
            val displayOrders = if (selectedTab == 0) ongoingOrders else historyOrders
            
            if (displayOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (selectedTab == 0) "暂无进行中的订单" else "暂无历史订单",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayOrders, key = { it.id ?: 0L }) { order ->
                        android.util.Log.d("OrderList", "OrderCard: id=${order.id}, merchantId=${order.merchantId}, merchant=${order.merchant}, merchantName=${order.merchantName}")
                        OrderCard(
                            order = order,
                            isHistory = selectedTab == 1,
                            onTrackOrder = { onTrackOrder(order.id ?: 0L) },
                            onReorder = { onReorder(order.id ?: 0L) },
                            onOrderDetail = { onOrderDetail(order.id ?: 0L) },
                            onCancelOrder = { onCancelOrder(order.id ?: 0L) },
                            onConfirmOrder = { onConfirmOrder(order.id ?: 0L) },
                            onReviewClick = { 
                                android.util.Log.d("OrderList", "onReviewClick called: orderId=${order.id}, merchantId=${order.merchantId}, hasReviewed=${order.hasReviewed}")
                                onReviewClick(order.id ?: 0L, order.merchantId ?: 0L, order.hasReviewed ?: false) 
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassHeader(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundLight.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                "我的订单",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "搜索")
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    isHistory: Boolean,
    onTrackOrder: () -> Unit,
    onReorder: () -> Unit,
    onOrderDetail: () -> Unit,
    onCancelOrder: () -> Unit,
    onConfirmOrder: () -> Unit,
    onReviewClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6))
                ) {
                    val imageUrl = remember(order.merchantLogo, order.merchant) {
                        order.merchantLogo?.takeIf { it.isNotBlank() }
                            ?: order.merchant?.logo?.takeIf { it.isNotBlank() }
                            ?: order.merchant?.banner?.takeIf { it.isNotBlank() }
                            ?: ""
                    }
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(imageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            order.merchantName ?: "商家",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1F2937),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (order.status) {
                                    0, 1 -> Icons.Filled.Schedule
                                    2, 3 -> Icons.Filled.DeliveryDining
                                    else -> Icons.Filled.CheckCircle
                                },
                                contentDescription = null,
                                tint = when (order.status) {
                                    0, 1 -> brandOrange
                                    2, 3 -> Color(0xFF10B981)
                                    else -> Color.Gray
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                getStatusText(order.status),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (order.status) {
                                    0, 1 -> brandOrange
                                    2, 3 -> Color(0xFF10B981)
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                    
                    if (isHistory) {
                        Text(
                            order.createTime ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    
                    Text(
                        order.items?.take(2)?.joinToString("、") { it.foodName ?: "" }
                            ?.let { if ((order.items?.size ?: 0) > 2) "$it 等${order.items?.size}件商品" else it }
                            ?: "订单商品",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Food images preview
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        order.items?.take(3)?.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF3F4F6))
                            ) {
                                AsyncImage(
                                    model = ImageUtils.getFullImageUrl(item.foodImage),
                                    contentDescription = item.foodName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
            
            if (!isHistory && order.status == 3) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "预计 ${getEstimatedTime()} 送达",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandOrange,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("总价", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    val displayPrice = order.finalAmount ?: order.totalAmount ?: BigDecimal.ZERO
                    android.util.Log.d("OrderPrice", "Order[${order.id}]: finalAmount=${order.finalAmount}, totalAmount=${order.totalAmount}, displayPrice=$displayPrice")
                    Text(
                        "¥$displayPrice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHistory) Color(0xFF6B7280) else Color(0xFF1F2937)
                    )
                }
                
                if (isHistory) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReorder,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = brandOrange),
                            border = androidx.compose.foundation.BorderStroke(1.dp, brandOrange)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("再来一单", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        if (order.status == 4) {
                            val hasReviewed = order.hasReviewed ?: false
                            Button(
                                onClick = onReviewClick,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                Icon(
                                    if (hasReviewed) Icons.Filled.Visibility else Icons.Filled.Star, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (hasReviewed) "查看评价" else "去评价", 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Allow cancel for 待支付(0) and 待接单(1)
                        if (order.status == 0 || order.status == 1) {
                            OutlinedButton(
                                onClick = onCancelOrder,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                            ) {
                                Text(if (order.status == 0) "取消订单" else "取消接单", fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = onTrackOrder,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                        ) {
                            Icon(
                                if (order.status < 2) Icons.Filled.Explore else Icons.Filled.NearMe,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (order.status < 2) "查看详情" else "追踪订单",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getStatusText(status: Int?): String {
    return when (status) {
        0 -> "待支付"
        1 -> "待接单"
        2 -> "已接单"
        3 -> "配送中"
        4 -> "已完成"
        5 -> "已取消"
        else -> "未知状态"
    }
}

private fun getEstimatedTime(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, 15)
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
}
