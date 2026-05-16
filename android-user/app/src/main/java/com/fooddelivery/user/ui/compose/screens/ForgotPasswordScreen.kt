package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fooddelivery.user.utils.MD5Util

private val brandOrange = Color(0xFFFF8C00)
private val textDark = Color(0xFF1D150C)

@Composable
fun ForgotPasswordScreen(
    onSendCode: (String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onResetPassword: (String, String, String) -> Unit = { _, _, _ -> },
    onClearError: () -> Unit = {},
    onBack: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null
) {
    var phoneText by remember { mutableStateOf("") }
    var codeText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(0) }
    
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }
    }
    
    LaunchedEffect(successMessage) {
        if (successMessage != null && currentStep == 1) {
            currentStep = 2
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // 返回按钮
            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("←", fontSize = 20.sp, color = textDark)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 标题
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(brandOrange, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔐", fontSize = 36.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("找回密码", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textDark)
            Text("通过手机号验证找回密码", fontSize = 14.sp, color = textDark.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(32.dp))

            // 步骤指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ResetStepDot(1, currentStep >= 0, brandOrange)
                Box(modifier = Modifier.width(40.dp).height(2.dp).background(if (currentStep >= 1) brandOrange else Color.Gray.copy(alpha = 0.3f)))
                ResetStepDot(2, currentStep >= 1, brandOrange)
                Box(modifier = Modifier.width(40.dp).height(2.dp).background(if (currentStep >= 2) brandOrange else Color.Gray.copy(alpha = 0.3f)))
                ResetStepDot(3, currentStep >= 2, brandOrange)
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (currentStep) {
                0 -> {
                    // 第一步：验证手机号
                    ForgotTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it.filter { c -> c.isDigit() }.take(11) },
                        placeholder = "请输入注册手机号",
                        icon = "📱"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ForgotTextField(
                        value = codeText,
                        onValueChange = { codeText = it.take(6) },
                        placeholder = "请输入验证码",
                        icon = "🔒",
                        trailing = {
                            Text(
                                if (countdown > 0) "${countdown}s" else "获取验证码", 
                                color = if (countdown > 0) Color.Gray else brandOrange, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier
                                    .background(brandOrange.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable(enabled = countdown == 0 && phoneText.length == 11) { 
                                        onSendCode(phoneText, { countdown = 60 }, { error -> })
                                    }
                            )
                        }
                    )
                }
                1 -> {
                    // 第二步：设置新密码
                    ForgotTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        placeholder = "请设置新密码（6-20位）",
                        icon = "🔒",
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ForgotTextField(
                        value = confirmPasswordText,
                        onValueChange = { confirmPasswordText = it },
                        placeholder = "请再次输入新密码",
                        icon = "🔒",
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                    )
                }
                2 -> {
                    // 第三步：完成
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth()
                    ) {
                        // 成功图标动画
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brandOrange.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 48.sp, color = brandOrange, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text("密码重置成功！", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textDark)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("您的密码已成功修改", fontSize = 14.sp, color = textDark.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 提示卡片
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(1.dp, brandOrange.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(20.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = brandOrange, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("新密码已生效", fontSize = 14.sp, color = textDark, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text("温馨提示：", fontSize = 12.sp, color = textDark.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("· 请妥善保管新密码", fontSize = 12.sp, color = textDark.copy(alpha = 0.5f))
                                Text("· 建议使用强密码以确保账号安全", fontSize = 12.sp, color = textDark.copy(alpha = 0.5f))
                                Text("· 如遇问题可联系客服", fontSize = 12.sp, color = textDark.copy(alpha = 0.5f))
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 大按钮返回登录
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(brandOrange)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("返回登录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 辅助文字
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("秒送餐 · 快人一步，美味即达", fontSize = 13.sp, color = textDark.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = Color.Red, fontSize = 12.sp)
            }

            successMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(msg, color = brandOrange, fontSize = 12.sp)
            }

            if (currentStep < 2) {
                Spacer(modifier = Modifier.height(24.dp))

                val canProceed = when (currentStep) {
                    0 -> phoneText.length == 11 && codeText.length >= 4
                    1 -> passwordText.length in 6..20 && confirmPasswordText == passwordText
                    else -> false
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canProceed) brandOrange else brandOrange.copy(alpha = 0.5f))
                        .clickable {
                            if (canProceed) {
                                if (currentStep < 1) {
                                    onSendCode(phoneText, { countdown = 60 }, { error -> })
                                    currentStep++
                                } else {
                                    onResetPassword(phoneText, codeText, passwordText)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (currentStep == 0) "下一步" else "确认修改", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (currentStep == 2) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brandOrange)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("返回登录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ResetStepDot(step: Int, isActive: Boolean, activeColor: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else Color.Gray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(step.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ForgotTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, brandOrange.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = Color.Gray, fontSize = 15.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None
            )
        }
        if (isPassword) {
            Box(modifier = Modifier.size(24.dp).clickable { onPasswordToggle() }, contentAlignment = Alignment.Center) {
                Text(if (passwordVisible) "👁" else "👁‍🗨", fontSize = 16.sp)
            }
        } else if (trailing != null) {
            trailing()
        }
    }
}
