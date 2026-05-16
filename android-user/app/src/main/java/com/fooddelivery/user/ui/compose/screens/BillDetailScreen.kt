package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.ui.compose.theme.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

private val brandOrange = Color(0xFFFF8C00)

data class BillDetail(
    val orderId: Long,
    val merchantName: String,
    val totalAmount: BigDecimal,
    val payAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val deliveryFee: BigDecimal,
    val packFee: BigDecimal,
    val payTime: Date,
    val payMethod: String,
    val status: String
)

@Composable
fun BillDetailScreen(
    billDetail: BillDetail? = null,
    onBack: () -> Unit = {},
    onReviewClick: (Long, Long) -> Unit = { _, _ -> }
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    
    val sampleBill = remember {
        BillDetail(
            orderId = 123456789012345L,
            merchantName = "Shake Shack 汉堡",
            totalAmount = BigDecimal("68.00"),
            payAmount = BigDecimal("53.00"),
            discountAmount = BigDecimal("15.00"),
            deliveryFee = BigDecimal("5.00"),
            packFee = BigDecimal("2.00"),
            payTime = Date(),
            payMethod = "支付宝",
            status = "支付成功"
        )
    }
    
    val displayBill = billDetail ?: sampleBill

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        Surface(
            color = brandOrange,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                Text(
                    text = "支付详情",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(36.dp),
                            color = SuccessGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = SuccessGreen
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            displayBill.status,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("¥", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(
                                String.format("%.2f", displayBill.payAmount),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            color = brandOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "已优惠 ¥${displayBill.discountAmount}",
                                fontSize = 13.sp,
                                color = brandOrange,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Store, contentDescription = null, tint = brandOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(displayBill.merchantName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("费用明细", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        BillRow("商品金额", "¥${displayBill.totalAmount.subtract(displayBill.deliveryFee).subtract(displayBill.packFee)}")
                        Spacer(modifier = Modifier.height(12.dp))
                        BillRow("配送费", "¥${displayBill.deliveryFee}")
                        if (displayBill.packFee > BigDecimal.ZERO) {
                            Spacer(modifier = Modifier.height(12.dp))
                            BillRow("包装费", "¥${displayBill.packFee}")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        BillRow("优惠减免", "-¥${displayBill.discountAmount}", valueColor = brandOrange)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("实付金额", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "¥${displayBill.payAmount}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = brandOrange
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("支付信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        BillRow("订单编号", "${displayBill.orderId}")
                        Spacer(modifier = Modifier.height(12.dp))
                        BillRow("支付方式", displayBill.payMethod)
                        Spacer(modifier = Modifier.height(12.dp))
                        BillRow("支付时间", dateFormat.format(displayBill.payTime))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = brandOrange)
                    ) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("申请退款")
                    }
                    
                    Button(
                        onClick = { displayBill?.let { onReviewClick(it.orderId, 0L) } },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("评价订单")
                    }
                }
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String, valueColor: Color = TextDark) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextGray)
        Text(value, fontSize = 14.sp, color = valueColor)
    }
}
