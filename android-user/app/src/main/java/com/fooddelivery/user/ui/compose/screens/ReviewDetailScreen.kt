package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.model.Review
import com.fooddelivery.user.ui.compose.theme.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)
private val textPrimary = Color(0xFF1D150C)
private val textSecondary = Color(0xFFA17745)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailScreen(
    review: Review? = null,
    orderId: Long = 0,
    merchantName: String = "",
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = backgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "评价详情",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    Surface(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = textPrimary.copy(alpha = 0.1f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundLight
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (review == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "加载中...",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = brandOrange.copy(alpha = 0.2f)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (review.userName?.firstOrNull() ?: "用").toString(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = brandOrange
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (review.isAnonymous == true) "匿名用户" else (review.userName ?: "用户"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = if (index < (review.rating ?: 0)) brandOrange else Color.LightGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = merchantName,
                                    fontSize = 14.sp,
                                    color = textSecondary
                                )
                                review.createTime?.let { time ->
                                    Text(
                                        text = time.take(10),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (!review.content.isNullOrBlank()) {
                            Text(
                                text = review.content,
                                fontSize = 14.sp,
                                color = textPrimary,
                                lineHeight = 22.sp
                            )
                        }
                        
                        if (review.tasteRating != null || review.portionRating != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                review.tasteRating?.let { taste: Int ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "口味",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row {
                                            repeat(5) { index: Int ->
                                                Icon(
                                                    Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = if (index < taste) brandOrange else Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                review.portionRating?.let { portion: Int ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "分量",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row {
                                            repeat(5) { index: Int ->
                                                Icon(
                                                    Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = if (index < portion) brandOrange else Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                val replyContent = review.replyContent ?: review.merchantReply
                if (!replyContent.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E6))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = brandOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "商家回复",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = brandOrange
                                )
                                review.replyTime?.let { time ->
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = time.take(10),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = replyContent,
                                fontSize = 14.sp,
                                color = textPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "评价状态",
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            val reviewStatus: Int = review.status ?: -1
                            val statusText = when (reviewStatus) {
                                0 -> "待审核"
                                1 -> "已通过"
                                2 -> "已拒绝"
                                else -> "未知"
                            }
                            val statusColor = when (reviewStatus) {
                                0 -> Color(0xFFFF9800)
                                1 -> Color(0xFF4CAF50)
                                2 -> Color(0xFFF44336)
                                else -> Color.Gray
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = statusColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = statusText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
