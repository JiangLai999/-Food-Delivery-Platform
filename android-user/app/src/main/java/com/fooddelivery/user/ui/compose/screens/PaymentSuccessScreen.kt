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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.ui.compose.theme.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

@Composable
fun PaymentSuccessScreen(
    orderId: String = "",
    amount: BigDecimal = BigDecimal.ZERO,
    paymentMethod: String = "微信支付",
    estimatedTime: String = "尽快送达",
    onBack: () -> Unit = {},
    onNavigateToOrder: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val orderNumber = remember { orderId.ifEmpty { "20231024${(1000..9999).random()}" } }
    val displayAmount = if (amount > BigDecimal.ZERO) amount else BigDecimal.ZERO

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .background(brandOrange.copy(alpha = 0.1f), RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 100.dp)
                .size(150.dp)
                .background(brandOrange.copy(alpha = 0.05f), RoundedCornerShape(50))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF1D150C))
                }
                Text(
                    "支付成功",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = RoundedCornerShape(48.dp),
                    color = brandOrange.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = brandOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "支付成功！",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D150C)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "订单正在飞速准备中",
                    fontSize = 14.sp,
                    color = brandOrange.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(40.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("支付金额", fontSize = 14.sp, color = Color(0xFF1D150C).copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("¥", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                                Text(
                                    String.format("%.2f", displayAmount),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandOrange
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(brandOrange.copy(alpha = 0.1f)))
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            DetailRow("订单编号", orderNumber)
                            DetailRow("支付方式", paymentMethod)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("预计送达", fontSize = 14.sp, color = Color(0xFF1D150C).copy(alpha = 0.6f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = brandOrange, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(estimatedTime, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onNavigateToOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("追踪订单", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brandOrange),
                    border = androidx.compose.foundation.BorderStroke(1.dp, brandOrange.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("返回首页", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF1D150C).copy(alpha = 0.6f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
