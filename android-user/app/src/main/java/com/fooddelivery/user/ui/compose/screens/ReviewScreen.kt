package com.fooddelivery.user.ui.compose.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.OrderItem
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.utils.ImageUtils

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)
private val textPrimary = Color(0xFF1D150C)
private val textSecondary = Color(0xFFA17745)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    orderId: String,
    orderItems: List<OrderItem> = emptyList(),
    orderTotal: String = "¥0",
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onSubmitReview: (Int, Int, String, List<String>, Boolean) -> Unit = { _, _, _, _, _ -> }
) {
    android.util.Log.d("ReviewScreen", "ReviewScreen rendered with orderId=$orderId, items=${orderItems.size}")
    
    var tasteRating by remember { mutableStateOf(0) }
    var portionRating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    val selectedImages = remember { mutableStateListOf<String>() }
    var isAnonymous by remember { mutableStateOf(false) }
    
    val glassCardColor = Color.White.copy(alpha = 0.7f)
    
    Scaffold(
        containerColor = backgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "菜品评价",
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
                                tint = textPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundLight.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = backgroundLight.copy(alpha = 0.9f),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        android.util.Log.d("ReviewScreen", "Submit clicked with tasteRating=$tasteRating")
                        onSubmitReview(tasteRating, portionRating, reviewText, selectedImages.toList(), isAnonymous)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                    shape = RoundedCornerShape(16.dp),
                    enabled = tasteRating > 0
                ) {
                    Text(
                        text = "提交评价",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dish Summary Card (Glassmorphism)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = glassCardColor,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 显示订单中的食品列表
                    if (orderItems.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(orderItems) { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(80.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF3F4F6))
                                    ) {
                                        if (!item.foodImage.isNullOrBlank()) {
                                            AsyncImage(
                                                model = ImageUtils.getFullImageUrl(item.foodImage),
                                                contentDescription = item.foodName,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.foodName ?: "菜品",
                                        fontSize = 10.sp,
                                        color = textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "x${item.quantity}",
                                        fontSize = 9.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = orderTotal,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = brandOrange
                            )
                        }
                    } else {
                        // 没有食品时显示默认
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                        )
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 96.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Surface(
                                color = brandOrange.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(24.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "待评价",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = brandOrange
                                    )
                                }
                            }
                            
                            Text(
                                text = "精品菜品",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            
                            Text(
                                text = orderTotal,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = brandOrange
                            )
                        }
                    }
                }
            }
            
            // Rating Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.5f),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Taste Rating
                    RatingRow(
                        label = "味道",
                        rating = tasteRating,
                        onRatingChange = { tasteRating = it }
                    )
                    
                    Divider(color = textPrimary.copy(alpha = 0.05f))
                    
                    // Portion Rating
                    RatingRow(
                        label = "分量",
                        rating = portionRating,
                        onRatingChange = { portionRating = it }
                    )
                }
            }
            
            // Text Input Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = {
                        Text(
                            text = "味道如何？包装还满意吗？写下你的真实评价吧...",
                            color = textSecondary.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.Transparent,
                        focusedBorderColor = brandOrange.copy(alpha = 0.1f),
                        unfocusedBorderColor = brandOrange.copy(alpha = 0.05f)
                    ),
                    maxLines = 6,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            // Media Upload Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "添加图片/视频",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(4) { index ->
                        val hasImage = index < selectedImages.size
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (hasImage) Color(0xFFF3F4F6)
                                    else brandOrange.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 2.dp,
                                    color = brandOrange.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasImage) {
                                AsyncImage(
                                    model = selectedImages[index],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AddAPhoto,
                                        contentDescription = "上传",
                                        tint = brandOrange,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "上传",
                                        fontSize = 10.sp,
                                        color = brandOrange,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Anonymous Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "匿名评价",
                        fontSize = 14.sp,
                        color = textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Switch(
                    checked = isAnonymous,
                    onCheckedChange = { isAnonymous = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = brandOrange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = brandOrange.copy(alpha = 0.2f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RatingRow(
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..5) {
                Icon(
                    if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (i <= rating) brandOrange else Color(0xFFE5E7EB),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onRatingChange(if (rating == i) i - 1 else i) }
                )
            }
        }
    }
}
