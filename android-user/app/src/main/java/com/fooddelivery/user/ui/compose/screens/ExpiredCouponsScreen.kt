package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.ui.compose.theme.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

private val brandOrange = Color(0xFFFF8C00)

data class ExpiredCoupon(
    val id: Long,
    val title: String,
    val description: String,
    val discountAmount: BigDecimal,
    val minOrderAmount: BigDecimal,
    val expiredAt: Date,
    val usedAt: Date? = null,
    val status: CouponStatus
)

enum class CouponStatus {
    USED, EXPIRED
}

@Composable
fun ExpiredCouponsScreen(
    coupons: List<ExpiredCoupon> = emptyList(),
    onBack: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) }
    
    val usedCoupons = remember(coupons) { coupons.filter { it.status == CouponStatus.USED } }
    val expiredCoupons = remember(coupons) { coupons.filter { it.status == CouponStatus.EXPIRED } }
    
    val displayCoupons = if (usedCoupons.isNotEmpty()) usedCoupons else expiredCoupons
    
    val sampleCoupons = remember {
        listOf(
            ExpiredCoupon(
                id = 1,
                title = "新用户专享",
                description = "全场通用",
                discountAmount = BigDecimal("15"),
                minOrderAmount = BigDecimal("30"),
                expiredAt = Date(),
                usedAt = Date(),
                status = CouponStatus.USED
            ),
            ExpiredCoupon(
                id = 2,
                title = "满减优惠",
                description = "限午餐时段使用",
                discountAmount = BigDecimal("10"),
                minOrderAmount = BigDecimal("50"),
                expiredAt = Date(),
                status = CouponStatus.EXPIRED
            ),
            ExpiredCoupon(
                id = 3,
                title = "配送费减免",
                description = "免配送费券",
                discountAmount = BigDecimal("5"),
                minOrderAmount = BigDecimal("20"),
                expiredAt = Date(),
                status = CouponStatus.EXPIRED
            )
        )
    }
    
    val finalCoupons = if (coupons.isEmpty()) sampleCoupons else displayCoupons

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    text = "历史优惠券",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (finalCoupons.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无历史优惠券", color = TextGray, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(finalCoupons, key = { it.id }) { coupon ->
                    ExpiredCouponCard(
                        coupon = coupon,
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpiredCouponCard(
    coupon: ExpiredCoupon,
    dateFormat: SimpleDateFormat
) {
    val isUsed = coupon.status == CouponStatus.USED
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.height(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(
                        if (isUsed) SuccessGreen.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "¥",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUsed) SuccessGreen else Color.Gray
                        )
                        Text(
                            "${coupon.discountAmount}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isUsed) SuccessGreen else Color.Gray
                        )
                    }
                    Text(
                        "满${coupon.minOrderAmount}可用",
                        fontSize = 10.sp,
                        color = if (isUsed) SuccessGreen.copy(alpha = 0.7f) else Color.Gray
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            coupon.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isUsed) TextDark else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = if (isUsed) SuccessGreen.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (isUsed) "已使用" else "已过期",
                                fontSize = 10.sp,
                                color = if (isUsed) SuccessGreen else Color.Gray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        coupon.description,
                        fontSize = 12.sp,
                        color = if (isUsed) TextGray else Color.Gray.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isUsed) "使用于 ${dateFormat.format(coupon.usedAt ?: Date())}" 
                        else "过期于 ${dateFormat.format(coupon.expiredAt)}",
                        fontSize = 11.sp,
                        color = TextHint
                    )
                }
            }
        }
    }
}
