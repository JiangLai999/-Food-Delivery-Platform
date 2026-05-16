package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.ui.compose.theme.*
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)
private val backgroundLight = Color(0xFFF8F7F5)

data class RechargeAmount(
    val amount: BigDecimal,
    val bonus: BigDecimal
)

@Composable
fun RechargeScreen(
    currentBalance: BigDecimal = BigDecimal("1250.80"),
    onBack: () -> Unit = {},
    onRecharge: (BigDecimal) -> Unit = {},
    onRechargeSuccess: () -> Unit = {}
) {
    var selectedAmount by remember { mutableStateOf<BigDecimal?>(BigDecimal("10")) }
    var customAmount by remember { mutableStateOf("") }
    
    val rechargeOptions = remember {
        listOf(
            RechargeAmount(BigDecimal("10"), BigDecimal("2")),
            RechargeAmount(BigDecimal("20"), BigDecimal("5")),
            RechargeAmount(BigDecimal("50"), BigDecimal("10")),
            RechargeAmount(BigDecimal("100"), BigDecimal("25")),
            RechargeAmount(BigDecimal("200"), BigDecimal("50")),
            RechargeAmount(BigDecimal("500"), BigDecimal("150"))
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
                    "充值余额",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            BalanceDisplayCard(balance = currentBalance)

            Text("选择充值金额", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(280.dp)
            ) {
                items(rechargeOptions) { option ->
                    AmountButton(
                        amount = option.amount,
                        bonus = option.bonus,
                        isSelected = selectedAmount == option.amount,
                        onClick = {
                            selectedAmount = option.amount
                            customAmount = ""
                        }
                    )
                }
            }

            Column {
                Text("其他金额", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customAmount,
                    onValueChange = { 
                        customAmount = it
                        selectedAmount = null
                    },
                    placeholder = { Text("请输入充值金额", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandOrange,
                        unfocusedBorderColor = Color(0xFFEADDCD),
                        focusedContainerColor = Color.White.copy(alpha = 0.7f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                    ),
                    leadingIcon = {
                        Text("¥", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    }
                )
            }

            PaymentMethodCard()

            Text(
                "点击立即充值即表示您已阅读并同意《用户充值协议》及《余额使用规则》",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BalanceDisplayCard(balance: BigDecimal) {
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
                        listOf(brandOrange, Color(0xFFFFAE42))
                    )
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
            )
            
            Column {
                Text("当前余额", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        String.format("%,.2f", balance),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("账户安全保障中", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountButton(
    amount: BigDecimal,
    bonus: BigDecimal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) brandOrange.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.7f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, brandOrange) else null
    ) {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¥", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isSelected) brandOrange else Color.Gray)
                Text(
                    "${amount.toInt()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) brandOrange else Color(0xFF1D150C)
                )
            }
            
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                color = if (isSelected) brandOrange else brandOrange.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "送 ¥${bonus.toInt()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodCard() {
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
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF07C160)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("微信支付", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("推荐使用，安全快捷", fontSize = 12.sp, color = Color.Gray)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("修改", color = brandOrange, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = brandOrange, modifier = Modifier.size(16.dp))
            }
        }
    }
}
