package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.User
import com.fooddelivery.user.ui.compose.theme.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

@Composable
fun ProfileScreen(
    user: User? = null,
    isLoggedIn: Boolean = false,
    couponCount: Int = 0,
    favoriteCount: Int = 0,
    footprintCount: Int = 0,
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onAddressClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onCouponClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onCustomerServiceClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        listOf(brandOrange.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { TopHeader(onSettingsClick) }

            item {
                UserInfoSection(
                    user = user,
                    isLoggedIn = isLoggedIn,
                    onEditProfileClick = onEditProfileClick,
                    onLoginClick = onLoginClick
                )
            }

            item { QuickStatsGrid(couponCount, favoriteCount, footprintCount) }

            item { OrdersSection(onOrderClick) }

            item {
                ServiceMenu(
                    onAddressClick = onAddressClick,
                    onCouponClick = onCouponClick,
                    onFavoritesClick = onFavoritesClick,
                    onWalletClick = onWalletClick,
                    onCustomerServiceClick = onCustomerServiceClick,
                    onLogout = onLogout,
                    isLoggedIn = isLoggedIn
                )
            }
        }
    }
}

@Composable
private fun TopHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "个人中心",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2216)
        )
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "设置", tint = Color(0xFF2D2216))
        }
    }
}

@Composable
private fun UserInfoSection(
    user: User?,
    isLoggedIn: Boolean,
    onEditProfileClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(4.dp, Color.White, CircleShape)
            ) {
                if (user?.avatar != null) {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = Color.Gray
                    )
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = brandOrange
            ) {
                Text(
                    "LV.5",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isLoggedIn && user != null) {
                Text(
                    user.nickname ?: user.phone ?: "用户",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2216)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFFFF7ED),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "超级尊享会员",
                            color = Color(0xFFB45309),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    "登录/注册",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2216)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "登录后享受更多权益",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }

        if (isLoggedIn) {
            Surface(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
            ) {
                Text(
                    "编辑资料",
                    color = brandOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            Button(
                onClick = onLoginClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
            ) {
                Text("去登录", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(couponCount: Int, favoriteCount: Int, footprintCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatItem(title = "优惠券", value = couponCount.toString(), modifier = Modifier.weight(1f))
        StatItem(title = "收藏夹", value = favoriteCount.toString(), modifier = Modifier.weight(1f))
        StatItem(title = "足迹", value = footprintCount.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatItem(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.65f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2216)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun OrdersSection(onOrderClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.65f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("我的订单", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2216))
                    TextButton(onClick = { onOrderClick("all") }) {
                        Text("全部订单", fontSize = 12.sp, color = Color.Gray)
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OrderStatusItem(Icons.Filled.Inventory, "待接单") { onOrderClick("pending") }      // status = 1
                    OrderStatusItem(Icons.Filled.LocalShipping, "配送中") { onOrderClick("delivering") } // status = 3
                    OrderStatusItem(Icons.Filled.RateReview, "待评价") { onOrderClick("unreviewed") }    // status = 4
                    OrderStatusItem(Icons.Filled.AssignmentReturn, "退款售后") { onOrderClick("refund") }   // status = 5
                }
            }
        }
    }
}

@Composable
private fun OrderStatusItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = brandOrange.copy(alpha = 0.05f),
            border = androidx.compose.foundation.BorderStroke(1.dp, brandOrange.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = brandOrange, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666))
    }
}

@Composable
private fun ServiceMenu(
    onAddressClick: () -> Unit,
    onCouponClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onWalletClick: () -> Unit,
    onCustomerServiceClick: () -> Unit,
    onLogout: () -> Unit,
    isLoggedIn: Boolean
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.65f)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                MenuItem(
                    icon = Icons.Filled.LocationOn,
                    title = "地址管理",
                    bgColor = Color(0xFFEEF2FF),
                    iconTint = Color(0xFF3B82F6),
                    onClick = onAddressClick
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.5f)).padding(horizontal = 16.dp))
                MenuItem(
                    icon = Icons.Filled.CardGiftcard,
                    title = "优惠券红包",
                    bgColor = Color(0xFFFFF7ED),
                    iconTint = brandOrange,
                    onClick = onCouponClick
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.5f)).padding(horizontal = 16.dp))
                MenuItem(
                    icon = Icons.Filled.Favorite,
                    title = "我的收藏",
                    bgColor = Color(0xFFFDF2F8),
                    iconTint = Color(0xFFEC4899),
                    onClick = onFavoritesClick
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.65f)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                MenuItem(
                    icon = Icons.Filled.HeadsetMic,
                    title = "客服中心",
                    bgColor = Color(0xFFECFDF5),
                    iconTint = Color(0xFF10B981),
                    onClick = onCustomerServiceClick
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.5f)).padding(horizontal = 16.dp))
                MenuItem(
                    icon = Icons.Filled.Policy,
                    title = "协议与资质",
                    bgColor = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF64748B),
                    onClick = {}
                )
            }
        }

        if (isLoggedIn) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.65f),
                onClick = onLogout
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "退出登录",
                        color = Color(0xFFEF4444),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    bgColor: Color = brandOrange.copy(alpha = 0.1f),
    iconTint: Color = brandOrange,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(14.dp),
            color = bgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF2D2216),
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFD1D5DB),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun Modifier.border(width: Dp, color: Color, shape: RoundedCornerShape): Modifier {
    return this.clip(shape).background(color, shape)
}
