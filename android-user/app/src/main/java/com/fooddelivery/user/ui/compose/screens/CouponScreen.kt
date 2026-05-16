package com.fooddelivery.user.ui.compose.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.ui.compose.theme.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class Coupon(
    val id: Long,
    val title: String,
    val description: String,
    val discountAmount: BigDecimal,
    val minOrderAmount: BigDecimal,
    val expireDate: String,
    val totalCount: Int,
    val remainCount: Int,
    val merchantId: Long?,
    val merchantName: String? = null,
    val isValid: Boolean = true
)

@Composable
fun CouponScreen(
    coupons: List<Coupon> = emptyList(),
    onBack: () -> Unit = {},
    onCouponSelect: (Coupon) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var promoCode by remember { mutableStateOf("") }
    
    val sampleCoupons = remember {
        listOf(
            Coupon(
                id = 1,
                title = "新人专享券",
                description = "新用户首单立减",
                discountAmount = BigDecimal("10"),
                minOrderAmount = BigDecimal("30"),
                expireDate = "2026-12-31",
                totalCount = 10000,
                remainCount = 8000,
                merchantId = null,
                merchantName = null
            ),
            Coupon(
                id = 2,
                title = "满减优惠券",
                description = "满50减15",
                discountAmount = BigDecimal("15"),
                minOrderAmount = BigDecimal("50"),
                expireDate = "2026-12-31",
                totalCount = 5000,
                remainCount = 3500,
                merchantId = null,
                merchantName = null
            ),
            Coupon(
                id = 3,
                title = "商家优惠券",
                description = "老北京炸酱面馆专属券",
                discountAmount = BigDecimal("5"),
                minOrderAmount = BigDecimal("25"),
                expireDate = "2026-12-31",
                totalCount = 1000,
                remainCount = 700,
                merchantId = 1,
                merchantName = "老北京炸酱面馆"
            )
        )
    }
    
    val displayCoupons = if (coupons.isEmpty()) sampleCoupons else coupons
    val validCoupons = displayCoupons.filter { it.isValid }
    val invalidCoupons = displayCoupons.filter { !it.isValid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = backgroundLight.copy(alpha = 0.95f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "返回",
                        tint = Color(0xFF1D1512)
                    )
                }
                Text(
                    "优惠专区",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color(0xFF1D1512)
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PromoCodeSection(
                    promoCode = promoCode,
                    onPromoCodeChange = { promoCode = it }
                )
            }

            item {
                CouponStatsSection(coupons = validCoupons)
            }

            item {
                Text(
                    "平台优惠券",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1512),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val platformCoupons = validCoupons.filter { it.merchantId == null }
            items(platformCoupons) { coupon ->
                PlatformCouponCard(
                    coupon = coupon,
                    onClick = { onCouponSelect(coupon) }
                )
            }

            item {
                Text(
                    "商家专属券",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1512),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val merchantCoupons = validCoupons.filter { it.merchantId != null }
            items(merchantCoupons) { coupon ->
                MerchantCouponCard(
                    coupon = coupon,
                    onClick = { onCouponSelect(coupon) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PromoCodeSection(
    promoCode: String,
    onPromoCodeChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ConfirmationNumber,
                    contentDescription = null,
                    tint = brandOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "有优惠码？",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1512)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = onPromoCodeChange,
                    placeholder = { 
                        Text("输入优惠码", color = Color(0xFFBFBFBF)) 
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandOrange,
                        unfocusedBorderColor = Color(0xFFE8E8E8),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Text("兑换", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun CouponStatsSection(coupons: List<Coupon>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "可用券",
            value = "${coupons.size}",
            icon = Icons.Filled.ConfirmationNumber,
            color = brandOrange
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "预计省",
            value = "¥${coupons.maxOfOrNull { it.discountAmount.toInt() } ?: 0}",
            icon = Icons.Filled.Savings,
            color = Color(0xFF52C41A)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "即将过期",
            value = "0",
            icon = Icons.Filled.Schedule,
            color = Color(0xFFEF5350)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1512)
            )
            Text(
                title,
                fontSize = 11.sp,
                color = Color(0xFF8C8C8C)
            )
        }
    }
}

@Composable
private fun PlatformCouponCard(
    coupon: Coupon,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(brandOrange, Color(0xFFFF6B00))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 20.dp)
                ) {
                    Text(
                        "¥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        "${coupon.discountAmount.toInt()}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "满${coupon.minOrderAmount.toInt()}可用",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = brandOrange.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "平台券",
                                fontSize = 10.sp,
                                color = brandOrange,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFBFBFBF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    coupon.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1D1512)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    coupon.description,
                    fontSize = 12.sp,
                    color = Color(0xFF8C8C8C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFBFBFBF),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "有效期至 ${coupon.expireDate}",
                            fontSize = 11.sp,
                            color = Color(0xFFBFBFBF)
                        )
                    }
                    Text(
                        "剩余 ${coupon.remainCount}张",
                        fontSize = 11.sp,
                        color = Color(0xFF52C41A)
                    )
                }
            }
        }
    }
}

@Composable
private fun MerchantCouponCard(
    coupon: Coupon,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF52C41A), Color(0xFF389E0D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 20.dp)
                ) {
                    Text(
                        "¥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        "${coupon.discountAmount.toInt()}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "满${coupon.minOrderAmount.toInt()}可用",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF52C41A).copy(alpha = 0.1f)
                        ) {
                            Text(
                                "商家券",
                                fontSize = 10.sp,
                                color = Color(0xFF52C41A),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFBFBFBF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    coupon.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1D1512)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    coupon.merchantName ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFF8C8C8C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFBFBFBF),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "有效期至 ${coupon.expireDate}",
                            fontSize = 11.sp,
                            color = Color(0xFFBFBFBF)
                        )
                    }
                    Text(
                        "剩余 ${coupon.remainCount}张",
                        fontSize = 11.sp,
                        color = Color(0xFF52C41A)
                    )
                }
            }
        }
    }
}
