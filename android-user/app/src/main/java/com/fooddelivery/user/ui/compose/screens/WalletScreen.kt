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
import com.fooddelivery.user.ui.compose.theme.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class Transaction(
    val id: Long,
    val title: String,
    val amount: BigDecimal,
    val time: Date,
    val type: TransactionType,
    val status: String = "交易成功"
)

enum class TransactionType {
    PAYMENT, RECHARGE, REFUND
}

@Composable
fun WalletScreen(
    balance: BigDecimal = BigDecimal("1250.80"),
    onBack: () -> Unit = {},
    onRecharge: () -> Unit = {},
    onWithdraw: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    
    val transactions = remember {
        listOf(
            Transaction(1, "订单支付 - 麦当劳", BigDecimal("-45.00"), Date(), TransactionType.PAYMENT),
            Transaction(2, "钱包充值", BigDecimal("200.00"), Date(), TransactionType.RECHARGE),
            Transaction(3, "满减优惠返还", BigDecimal("5.00"), Date(), TransactionType.REFUND),
            Transaction(4, "订单支付 - 喜茶", BigDecimal("-28.50"), Date(), TransactionType.PAYMENT)
        )
    }

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
                    "我的钱包",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Help, contentDescription = "帮助")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                BalanceCard(
                    balance = balance,
                    onRecharge = onRecharge,
                    onWithdraw = onWithdraw
                )
            }

            item {
                QuickEntryGrid()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("交易明细", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { }) {
                        Text("筛选", color = brandOrange, fontSize = 14.sp)
                        Icon(Icons.Filled.FilterList, contentDescription = null, tint = brandOrange, modifier = Modifier.size(16.dp))
                    }
                }
            }

            items(transactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    dateFormat = dateFormat
                )
            }

            item {
                Text(
                    "仅展示近3个月的交易记录",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun BalanceCard(
    balance: BigDecimal,
    onRecharge: () -> Unit,
    onWithdraw: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(brandOrange.copy(alpha = 0.9f), Color(0xFFFFB347))
                    )
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
            )
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("总余额 (元)", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "¥ ${String.format("%,.2f", balance)}",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onRecharge,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, tint = brandOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("充值", color = brandOrange, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onWithdraw,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("提现", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickEntryGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickEntryItem(
            icon = Icons.Filled.CreditCard,
            label = "银行卡",
            bgColor = Color(0xFFEEF2FF),
            iconTint = Color(0xFF2563EB),
            modifier = Modifier.weight(1f)
        )
        QuickEntryItem(
            icon = Icons.Filled.CardGiftcard,
            label = "红包",
            bgColor = Color(0xFFFEE2E2),
            iconTint = Color(0xFFDC2626),
            badge = "5个",
            modifier = Modifier.weight(1f)
        )
        QuickEntryItem(
            icon = Icons.Filled.ConfirmationNumber,
            label = "优惠券",
            bgColor = brandOrange.copy(alpha = 0.1f),
            iconTint = brandOrange,
            badge = "12张",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickEntryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    iconTint: Color,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = bgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (badge != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTint
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    dateFormat: SimpleDateFormat
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = when (transaction.type) {
                    TransactionType.PAYMENT -> backgroundLight
                    TransactionType.RECHARGE -> brandOrange.copy(alpha = 0.1f)
                    TransactionType.REFUND -> Color(0xFFECFDF5)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (transaction.type) {
                            TransactionType.PAYMENT -> Icons.Filled.ShoppingBag
                            TransactionType.RECHARGE -> Icons.Filled.AccountBalanceWallet
                            TransactionType.REFUND -> Icons.Filled.Redeem
                        },
                        contentDescription = null,
                        tint = when (transaction.type) {
                            TransactionType.PAYMENT -> Color.Gray
                            TransactionType.RECHARGE -> brandOrange
                            TransactionType.REFUND -> Color(0xFF10B981)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    dateFormat.format(transaction.time),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (transaction.amount >= BigDecimal.ZERO) "+¥${transaction.amount}"
                    else "¥${transaction.amount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = when (transaction.type) {
                        TransactionType.PAYMENT -> Color(0xFF1D150C)
                        TransactionType.RECHARGE -> brandOrange
                        TransactionType.REFUND -> Color(0xFF10B981)
                    }
                )
                Text(
                    transaction.status,
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
