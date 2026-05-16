package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fooddelivery.user.model.Address
import com.fooddelivery.user.model.CartItem
import com.fooddelivery.user.model.Coupon
import com.fooddelivery.user.ui.compose.theme.*
import com.fooddelivery.user.ui.compose.components.LoadingIndicator
import com.fooddelivery.user.utils.ImageUtils
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)

data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val enabled: Boolean = true
)

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem> = emptyList(),
    merchantId: Long = 0L,
    merchantName: String = "",
    address: Address? = null,
    deliveryFee: BigDecimal = BigDecimal.ZERO,
    packFee: BigDecimal = BigDecimal.ZERO,
    availableCoupons: List<Coupon> = emptyList(),
    selectedCoupon: Coupon? = null,
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onSelectAddress: () -> Unit = {},
    onSelectCoupon: (Coupon?) -> Unit = {},
    onPay: (String) -> Unit = {}
) {
    var selectedPayment by remember { mutableStateOf("alipay") }
    var remark by remember { mutableStateOf("") }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var showCouponDialog by remember { mutableStateOf(false) }
    
    val paymentMethods = listOf(
        PaymentMethod("alipay", "支付宝", Icons.Filled.AccountBalanceWallet, Color(0xFF1677FF)),
        PaymentMethod("wechat", "微信支付", Icons.AutoMirrored.Filled.Chat, Color(0xFF07C160)),
        PaymentMethod("balance", "余额支付", Icons.Filled.AccountBalance, Color(0xFFFF9500), enabled = false)
    )
    
    val subtotal = cartItems.fold(BigDecimal.ZERO) { total, item ->
        total.add(item.getSubtotal())
    }
    val couponDiscount = selectedCoupon?.amount ?: BigDecimal.ZERO
    val totalAmount = subtotal.add(deliveryFee).add(packFee).subtract(couponDiscount)

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
                    text = "确认订单",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AddressCard(
                    address = address,
                    onClick = onSelectAddress
                )
            }

            item {
                OrderItemsCard(
                    cartItems = cartItems,
                    merchantName = merchantName
                )
            }

            item {
                CouponCard(
                    selectedCoupon = selectedCoupon,
                    availableCoupons = availableCoupons,
                    onClick = { showCouponDialog = true }
                )
            }

            item {
                PaymentMethodsCard(
                    paymentMethods = paymentMethods,
                    selectedPayment = selectedPayment,
                    onSelectPayment = { selectedPayment = it }
                )
            }

            item {
                RemarkCard(
                    remark = remark,
                    onClick = { showRemarkDialog = true }
                )
            }

             item {
                 FeeDetailCard(
                     subtotal = subtotal,
                     deliveryFee = deliveryFee,
                     packFee = packFee,
                     couponDiscount = couponDiscount
                 )
             }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        CheckoutBottomBar(
            totalAmount = totalAmount,
            isLoading = isLoading,
            hasAddress = address != null,
            remark = remark,
            onPay = { r -> onPay(r) }
        )
    }
    
    if (showRemarkDialog) {
        RemarkDialog(
            initialRemark = remark,
            onDismiss = { showRemarkDialog = false },
            onConfirm = { 
                remark = it
                showRemarkDialog = false
            }
        )
    }
    
    if (showCouponDialog) {
        CouponSelectionDialog(
            availableCoupons = availableCoupons,
            selectedCoupon = selectedCoupon,
            merchantId = merchantId,
            subtotal = subtotal,
            onDismiss = { showCouponDialog = false },
            onSelectCoupon = { coupon ->
                onSelectCoupon(coupon)
                showCouponDialog = false
            }
        )
    }
}

@Composable
private fun AddressCard(
    address: Address?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = brandOrange.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocationOn, tint = brandOrange, contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (address != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(address.receiverName ?: "收货人", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(address.receiverPhone ?: "", fontSize = 14.sp, color = TextGray)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        address.getFullAddress(),
                        fontSize = 13.sp,
                        color = TextGray,
                        maxLines = 2
                    )
                } else {
                    Text("请选择收货地址", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = brandOrange)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("点击添加收货地址", fontSize = 13.sp, color = TextGray)
                }
            }
            
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
        }
    }
}

@Composable
private fun OrderItemsCard(
    cartItems: List<CartItem>,
    merchantName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = brandOrange.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Store, contentDescription = null, tint = brandOrange, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(merchantName.ifEmpty { "商家" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            cartItems.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.LightGray)
                    ) {
                        if (item.foodImage != null) {
                            AsyncImage(
                                model = ImageUtils.getFullImageUrl(item.foodImage),
                                contentDescription = item.foodName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center).size(24.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.foodName ?: "商品", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Text("x${item.quantity}", fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("¥${item.getSubtotal()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodsCard(
    paymentMethods: List<PaymentMethod>,
    selectedPayment: String,
    onSelectPayment: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("支付方式", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            paymentMethods.forEach { method ->
                PaymentMethodItem(
                    method = method,
                    isSelected = selectedPayment == method.id,
                    onSelect = { if (method.enabled) onSelectPayment(method.id) }
                )
                if (method != paymentMethods.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected && method.enabled) 1.5.dp else 0.dp,
                color = if (isSelected && method.enabled) brandOrange else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (isSelected && method.enabled) brandOrange.copy(alpha = 0.05f)
                else Color(0xFFF8F8F8)
            )
            .clickable(enabled = method.enabled, onClick = onSelect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = method.iconBgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(method.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                method.name,
                fontWeight = if (isSelected && method.enabled) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp,
                color = if (method.enabled) TextDark else Color.Gray
            )
            if (!method.enabled) {
                Text("余额不足", fontSize = 11.sp, color = TextHint)
            }
        }
        if (method.enabled) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = brandOrange)
            )
        }
    }
}

@Composable
private fun RemarkCard(
    remark: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.EditNote, contentDescription = null, tint = brandOrange, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                if (remark.isEmpty()) "添加备注" else remark,
                fontSize = 14.sp,
                color = if (remark.isEmpty()) TextGray else TextDark,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
        }
    }
}

@Composable
private fun CouponCard(
    selectedCoupon: Coupon?,
    availableCoupons: List<Coupon>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = brandOrange, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (selectedCoupon != null) {
                    Text(selectedCoupon.title ?: "优惠券", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = brandOrange)
                    Text("-¥${selectedCoupon.amount}", fontSize = 12.sp, color = TextGray)
                } else {
                    Text(
                        if (availableCoupons.isNotEmpty()) "请选择优惠券" else "暂无优惠券可用",
                        fontSize = 14.sp,
                        color = if (availableCoupons.isNotEmpty()) TextDark else TextGray
                    )
                    if (availableCoupons.isNotEmpty()) {
                        Text("${availableCoupons.size}张优惠券可用", fontSize = 12.sp, color = TextGray)
                    }
                }
            }
            if (selectedCoupon != null) {
                TextButton(onClick = { onClick() }) {
                    Text("更换", color = brandOrange, fontSize = 12.sp)
                }
            } else {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
            }
        }
    }
}

@Composable
private fun FeeDetailCard(
    subtotal: BigDecimal,
    deliveryFee: BigDecimal,
    packFee: BigDecimal,
    couponDiscount: BigDecimal = BigDecimal.ZERO
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("费用明细", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            FeeRow("商品小计", "¥$subtotal")
            Spacer(modifier = Modifier.height(12.dp))
            FeeRow(
                "配送费",
                if (deliveryFee > BigDecimal.ZERO) "¥$deliveryFee" else "免配送费",
                valueColor = if (deliveryFee > BigDecimal.ZERO) TextDark else SuccessGreen
            )
            if (packFee > BigDecimal.ZERO) {
                Spacer(modifier = Modifier.height(12.dp))
                FeeRow("包装费", "¥$packFee")
            }
            if (couponDiscount > BigDecimal.ZERO) {
                Spacer(modifier = Modifier.height(12.dp))
                FeeRow("优惠券", "-¥$couponDiscount", valueColor = SuccessGreen)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("订单总计", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    Text(
                        String.format("%.2f", subtotal.add(deliveryFee).add(packFee).subtract(couponDiscount)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = brandOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun FeeRow(label: String, value: String, valueColor: Color = TextDark) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp)
    }
}

@Composable
private fun CheckoutBottomBar(
    totalAmount: BigDecimal,
    isLoading: Boolean,
    hasAddress: Boolean,
    remark: String,
    onPay: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("实付金额", fontSize = 12.sp, color = TextGray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    Text(
                        String.format("%.2f", totalAmount),
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = brandOrange
                    )
                }
            }
            
            Button(
                onClick = { onPay(remark) },
                modifier = Modifier.width(140.dp).height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandOrange,
                    disabledContainerColor = Color.Gray
                ),
                enabled = !isLoading && hasAddress
            ) {
                if (isLoading) {
                    Text("...", color = Color.White, fontSize = 18.sp)
                } else {
                    Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("立即支付", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RemarkDialog(
    initialRemark: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var remark by remember { mutableStateOf(initialRemark) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加备注", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                placeholder = { Text("有什么要交代的？例如：少放辣、多加葱") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandOrange,
                    unfocusedBorderColor = DividerColor
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(remark) }) {
                Text("确定", color = brandOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextGray)
            }
        }
    )
}

@Composable
private fun CouponSelectionDialog(
    availableCoupons: List<Coupon>,
    selectedCoupon: Coupon?,
    merchantId: Long = 0L,
    subtotal: BigDecimal,
    onDismiss: () -> Unit,
    onSelectCoupon: (Coupon?) -> Unit
) {
    val usableCoupons = availableCoupons.filter { coupon ->
        coupon.isAvailable() &&
        subtotal >= (coupon.minSpend ?: BigDecimal.ZERO) &&
        (coupon.merchantId == null || coupon.merchantId <= 0 || coupon.merchantId == merchantId)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择优惠券", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCoupon(null) },
                    color = if (selectedCoupon == null) brandOrange.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCoupon == null,
                            onClick = { onSelectCoupon(null) },
                            colors = RadioButtonDefaults.colors(selectedColor = brandOrange)
                        )
                        Text("不使用优惠券", fontWeight = FontWeight.Medium)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (usableCoupons.isEmpty()) {
                    Text("暂无可用优惠券", color = TextGray, modifier = Modifier.padding(16.dp))
                } else {
                    usableCoupons.forEach { coupon ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelectCoupon(coupon) },
                            color = if (selectedCoupon?.id == coupon.id) brandOrange.copy(alpha = 0.1f) else Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedCoupon?.id == coupon.id) brandOrange else DividerColor
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCoupon?.id == coupon.id,
                                    onClick = { onSelectCoupon(coupon) },
                                    colors = RadioButtonDefaults.colors(selectedColor = brandOrange)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(coupon.title ?: "优惠券", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "满${coupon.minSpend ?: BigDecimal.ZERO}减${coupon.amount}",
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }
                                Text(
                                    "-¥${coupon.amount}",
                                    color = brandOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定", color = brandOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextGray)
            }
        }
    )
}
