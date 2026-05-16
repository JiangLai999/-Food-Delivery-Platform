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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.model.SystemNotice
import com.fooddelivery.user.ui.compose.theme.*
import java.util.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class NotificationItem(
    val id: Long,
    val title: String,
    val content: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    ORDER, PROMO, SYSTEM
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem> = emptyList(),
    systemNotices: List<SystemNotice> = emptyList(),
    onBack: () -> Unit = {},
    onNotificationClick: (Long) -> Unit = {},
    onMarkAllRead: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val sampleNotifications = remember {
        listOf(
            NotificationItem(1, "您的订单正在配送中", "骑手已取货，正快马加鞭向您赶来，预计 15 分钟内送达。", "刚刚", NotificationType.ORDER, false),
            NotificationItem(2, "专属满减优惠券待领取", "老板大气！送您一张「满40减20」大额券，仅限今日使用。", "10分钟前", NotificationType.PROMO, false),
            NotificationItem(3, "订单已送达", "祝您用餐愉快！别忘了给骑手一个小星星评价哦。", "45分钟前", NotificationType.ORDER, true),
            NotificationItem(4, "系统版本更新", "v2.4.0 版本发布：优化了结算页面的加载速度，修复了已知的问题。", "昨天", NotificationType.SYSTEM, true)
        )
    }
    
    val displayNotifications = if (notifications.isEmpty()) sampleNotifications else notifications
    
    val allNotifications = remember(displayNotifications, systemNotices) {
        val systemItems = systemNotices.map { notice ->
            NotificationItem(
                id = notice.id ?: 0L,
                title = notice.title ?: "",
                content = notice.content ?: "",
                time = notice.createTime ?: "",
                type = when (notice.type) {
                    0 -> NotificationType.SYSTEM
                    1 -> NotificationType.PROMO
                    2 -> NotificationType.SYSTEM
                    else -> NotificationType.SYSTEM
                },
                isRead = notice.isRead == 1
            )
        }
        displayNotifications + systemItems
    }
    
    // 根据选中tab过滤通知
    val filteredNotifications = remember(allNotifications, selectedTab) {
        when (selectedTab) {
            0 -> allNotifications // 全部
            1 -> allNotifications.filter { it.type == NotificationType.ORDER } // 订单动态
            2 -> allNotifications.filter { it.type == NotificationType.PROMO } // 优惠活动
            3 -> allNotifications.filter { it.type == NotificationType.SYSTEM } // 系统通知
            else -> allNotifications
        }
    }
    
    val unreadCount = allNotifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    "通知中心",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = onMarkAllRead,
                    shape = RoundedCornerShape(20.dp),
                    color = brandOrange.copy(alpha = 0.1f)
                ) {
                    Text(
                        "全部已读",
                        color = brandOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("全部", "订单动态", "优惠活动", "系统通知").forEachIndexed { index, tab ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(tab, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = brandOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredNotifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = { onNotificationClick(notification.id) }
                )
            }
            
            if (filteredNotifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "暂无通知",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (!notification.isRead) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(32.dp)
                        .background(brandOrange, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = when (notification.type) {
                    NotificationType.ORDER -> brandOrange.copy(alpha = 0.2f)
                    NotificationType.PROMO -> Color(0xFFFED7AA)
                    NotificationType.SYSTEM -> Color(0xFFDBEAFE)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (notification.type) {
                            NotificationType.ORDER -> Icons.Filled.DeliveryDining
                            NotificationType.PROMO -> Icons.Filled.Redeem
                            NotificationType.SYSTEM -> Icons.Filled.Settings
                        },
                        contentDescription = null,
                        tint = when (notification.type) {
                            NotificationType.ORDER -> brandOrange
                            NotificationType.PROMO -> Color(0xFFF97316)
                            NotificationType.SYSTEM -> Color(0xFF2563EB)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        notification.time,
                        fontSize = 11.sp,
                        color = brandOrange.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    notification.content,
                    fontSize = 13.sp,
                    color = Color(0xFFA17745),
                    maxLines = 2
                )
                
                if (notification.type == NotificationType.ORDER && !notification.isRead) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("查看物流", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = brandOrange),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("联系骑手", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
