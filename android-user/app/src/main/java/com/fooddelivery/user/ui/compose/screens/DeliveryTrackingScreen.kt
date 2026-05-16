package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.Order
import com.fooddelivery.user.ui.compose.theme.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class DeliveryStatus(
    val title: String,
    val description: String,
    val time: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

@Composable
fun DeliveryTrackingScreen(
    order: Order? = null,
    orderId: String = "",
    riderLocation: com.fooddelivery.user.model.RiderLocation? = null,
    onBack: () -> Unit = {},
    onViewDetails: () -> Unit = {}
) {
    val currentPhase = riderLocation?.phase ?: "delivering"
    val distanceToUser = riderLocation?.distanceToUser ?: 0
    val statusDescription = riderLocation?.description ?: ""
    
    val deliveryStatuses = remember(currentPhase) {
        when (currentPhase) {
            "going_to_merchant" -> listOf(
                DeliveryStatus("已下单", "您的订单已成功提交", "12:30", true, false),
                DeliveryStatus("备餐中", "商家正在为您准备餐品", "12:32", true, false),
                DeliveryStatus("前往商家", "骑手正在赶往商家", "12:35", true, true),
                DeliveryStatus("正在取餐", "骑手正在取餐", "--", false, false),
                DeliveryStatus("配送中", "骑手正在为您配送", "--", false, false),
                DeliveryStatus("已送达", "骑手已送达", "--", false, false)
            )
            "picking_up" -> listOf(
                DeliveryStatus("已下单", "您的订单已成功提交", "12:30", true, false),
                DeliveryStatus("备餐中", "商家正在为您准备餐品", "12:32", true, false),
                DeliveryStatus("前往商家", "骑手已到达商家", "12:35", true, false),
                DeliveryStatus("正在取餐", "骑手正在取餐", "12:36", true, true),
                DeliveryStatus("配送中", "骑手正在为您配送", "--", false, false),
                DeliveryStatus("已送达", "骑手已送达", "--", false, false)
            )
            else -> listOf(
                DeliveryStatus("已下单", "您的订单已成功提交", "12:30", true, false),
                DeliveryStatus("备餐中", "商家正在为您准备餐品", "12:32", true, false),
                DeliveryStatus("前往商家", "骑手已到达商家", "12:35", true, false),
                DeliveryStatus("正在取餐", "骑手已取餐完成", "12:36", true, false),
                DeliveryStatus("配送中", "骑手正在为您配送", "12:55", true, true),
                DeliveryStatus("已送达", "骑手已送达", "--", false, false)
            )
        }
    }
    
    val estimatedTime = remember(riderLocation?.estimatedTime) {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MINUTE, riderLocation?.estimatedTime ?: 2)
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(calendar.time)
    }
    
    val displayOrder = order ?: Order().apply {
        orderNo = "FD202401150001"
        riderName = riderLocation?.riderName ?: "骑手"
        status = 3
    }
    
    val statusTitle = when (currentPhase) {
        "going_to_merchant" -> "前往商家"
        "picking_up" -> "正在取餐"
        else -> "配送中"
    }
    
    val statusDesc = when (currentPhase) {
        "going_to_merchant" -> "正在火速赶往商家取餐"
        "picking_up" -> "骑手正在商家取餐"
        else -> if (statusDescription.isNotEmpty()) statusDescription else "距离您还有 ${distanceToUser}米"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8E4DF))
        ) {
            AsyncImage(
                model = null,
                contentDescription = "配送地图",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                backgroundLight.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Transparent,
                                backgroundLight.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-80).dp, y = (-60).dp)
                    .size(300.dp)
                    .background(brandOrange.copy(alpha = 0.15f), RoundedCornerShape(50))
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "订单号: #${displayOrder.orderNo?.takeLast(7) ?: "9821450"}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Surface(
                    onClick = { },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Share, contentDescription = "分享")
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = brandOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = brandOrange, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("预计送达时间", fontSize = 12.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(estimatedTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                                Text(" (约15分钟)", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.7f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    statusTitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D150C)
                                )
                                Text(
                                    statusDesc,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            Surface(
                                color = brandOrange.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "配送中",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ProgressStepper(deliveryStatuses)

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.2f)))

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.LightGray
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 2.dp, y = 2.dp)
                                            .size(12.dp)
                                            .background(Color(0xFF22C55E), RoundedCornerShape(6.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(displayOrder.riderName ?: "骑手", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = brandOrange, modifier = Modifier.size(12.dp))
                                        Text(" 4.9", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                                    }
                                    Text(
                                        "专业骑手 • 已配送 2451 单",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    onClick = onViewDetails,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("查看订单详情", fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStepper(statuses: List<DeliveryStatus>) {
    val currentStep = statuses.indexOfFirst { it.isCurrent }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.Center)
                .background(Color(0xFFE5E7EB))
        )
        
        if (currentStep >= 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((currentStep + 1).toFloat() / statuses.size)
                    .height(2.dp)
                    .align(Alignment.CenterStart)
                    .background(brandOrange)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        statuses.forEach { status ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        status.isCurrent -> brandOrange
                        status.isCompleted -> brandOrange
                        else -> Color(0xFFE5E7EB)
                    },
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.White)
                ) {}
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    status.title,
                    fontSize = 10.sp,
                    fontWeight = if (status.isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        status.isCurrent -> brandOrange
                        status.isCompleted -> Color.Gray
                        else -> Color(0xFF9CA3AF)
                    }
                )
            }
        }
    }
}
