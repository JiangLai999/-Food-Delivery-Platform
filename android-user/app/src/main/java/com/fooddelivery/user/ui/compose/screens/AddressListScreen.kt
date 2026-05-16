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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.model.Address
import com.fooddelivery.user.ui.compose.theme.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class AddressItem(
    val id: Long,
    val label: String,
    val labelIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelColor: Color,
    val isDefault: Boolean,
    val contactName: String,
    val contactPhone: String,
    val fullAddress: String
)

@Composable
fun AddressListScreen(
    addresses: List<Address> = emptyList(),
    onBack: () -> Unit = {},
    onAddAddress: () -> Unit = {},
    onEditAddress: (Long) -> Unit = {},
    onDeleteAddress: (Long) -> Unit = {},
    onSelectAddress: (Long) -> Unit = {}
) {
    val displayAddresses = addresses.map { addr ->
        AddressItem(
            id = addr.id ?: 0L,
            label = addr.tag ?: "其他",
            labelIcon = when (addr.tag) {
                "家" -> Icons.Filled.Home
                "公司" -> Icons.Filled.Work
                else -> Icons.Filled.LocationOn
            },
            labelColor = if (addr.isDefault()) brandOrange else brandOrange.copy(alpha = 0.7f),
            isDefault = addr.isDefault(),
            contactName = addr.receiverName ?: "",
            contactPhone = addr.receiverPhone ?: "",
            fullAddress = addr.getFullAddress()
        )
    }

    val isEmpty = displayAddresses.isEmpty()

    Box(modifier = Modifier.fillMaxSize().background(backgroundLight)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = backgroundLight.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Text(
                        "我的地址",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEmpty) {
                    item {
                        EmptyAddressState(onAddAddress = onAddAddress)
                    }
                } else {
                    items(displayAddresses) { address ->
                        AddressCard(
                            address = address,
                            onEdit = { onEditAddress(address.id) },
                            onDelete = { onDeleteAddress(address.id) },
                            onClick = { onSelectAddress(address.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(128.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, backgroundLight)
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "添加更多地址，让公司、健身房或好友家的下单更快速。",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA17745),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White.copy(alpha = 0.4f)
        ) {
            Button(
                onClick = onAddAddress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
            ) {
                Icon(Icons.Filled.AddLocationAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加新地址", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: AddressItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (address.isDefault) brandOrange else brandOrange.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                address.labelIcon,
                                contentDescription = null,
                                tint = if (address.isDefault) Color.White else brandOrange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(address.label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (address.isDefault) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = brandOrange.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "默认",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = brandOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = Color(0xFFA17745))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color(0xFFEF4444).copy(alpha = 0.7f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column {
                Text(
                    "${address.contactName} | ${address.contactPhone}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    address.fullAddress,
                    fontSize = 14.sp,
                    color = Color(0xFFA17745),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyAddressState(onAddAddress: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        Icon(
            Icons.Filled.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "暂无收货地址",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2216)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "添加收货地址，享受便捷配送服务",
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAddAddress,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加收货地址", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
