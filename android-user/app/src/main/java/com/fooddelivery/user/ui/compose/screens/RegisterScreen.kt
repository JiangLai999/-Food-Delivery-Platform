package com.fooddelivery.user.ui.compose.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fooddelivery.user.utils.MD5Util
import com.fooddelivery.user.R

private val brandOrange = Color(0xFFFF8C00)
private val textDark = Color(0xFF1D150C)

@Composable
fun RegisterScreen(
    onRegister: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onSendCode: (String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onClearError: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onBack: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    val textDark = Color(0xFF1D150C)
    
    var phoneText by remember { mutableStateOf("") }
    var codeText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var nicknameText by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(0) }
    
    // 尝试加载商标图标
    val logoBitmap = remember {
        try {
            val drawable = context.getDrawable(R.drawable.ic_launcher)
            drawable?.toBitmap(144, 144)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        // 背景装饰
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0x40FF8C00),
                            Color(0x20FF8C00),
                            Color(0xFF1A1A2E)
                        )
                    )
                )
        )
        
        // 装饰圆形
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (-40).dp)
                .size(250.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0x30FF8C00), Color(0x00FF8C00))
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 80.dp)
                .size(200.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0x25FF8C00), Color(0x00FF8C00))
                    )
                )
        )

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
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("←", fontSize = 20.sp, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(brandOrange.copy(alpha = 0.15f))
                    .border(2.dp, brandOrange.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (logoBitmap != null) {
                    Image(
                        bitmap = logoBitmap,
                        contentDescription = "秒送餐",
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Text("秒", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Text("新用户注册", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("加入秒送餐，发现更多美食", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(32.dp))

            // 步骤指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StepDot(1, currentStep >= 0, brandOrange)
                Box(modifier = Modifier.width(40.dp).height(2.dp).background(if (currentStep >= 1) brandOrange else Color.White.copy(alpha = 0.2f)))
                StepDot(2, currentStep >= 1, brandOrange)
                Box(modifier = Modifier.width(40.dp).height(2.dp).background(if (currentStep >= 2) brandOrange else Color.White.copy(alpha = 0.2f)))
                StepDot(3, currentStep >= 2, brandOrange)
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (currentStep) {
                0 -> {
                    RegisterTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it.filter { c -> c.isDigit() }.take(11) },
                        placeholder = "请输入手机号",
                        icon = "📱",
                        inputColor = Color(0xFF1D150C),
                        placeholderColor = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    RegisterTextField(
                        value = codeText,
                        onValueChange = { codeText = it.take(6) },
                        placeholder = "请输入验证码",
                        icon = "🔒",
                        inputColor = Color(0xFF1D150C),
                        placeholderColor = Color.Gray,
                        trailing = {
                            Text(
                                if (countdown > 0) "${countdown}s" else "获取验证码", 
                                color = if (countdown > 0) Color.Gray else brandOrange, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier
                                    .background(
                                        if (countdown == 0 && phoneText.length == 11) Color(0xFFFFF3E0) else Color.Transparent, 
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable(enabled = countdown == 0 && phoneText.length == 11) { 
                                        onSendCode(phoneText, { 
                                            countdown = 60
                                            android.widget.Toast.makeText(context, "验证码: 123456", android.widget.Toast.LENGTH_LONG).show()
                                        }, { error -> 
                                            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                                        })
                                    }
                            )
                        }
                    )
                }
                1 -> {
                    RegisterTextField(
                        value = nicknameText,
                        onValueChange = { nicknameText = it.take(20) },
                        placeholder = "请输入昵称（选填）",
                        icon = "👤",
                        inputColor = Color(0xFF1D150C),
                        placeholderColor = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    RegisterTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        placeholder = "请设置密码（6-20位）",
                        icon = "🔒",
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        inputColor = Color(0xFF1D150C),
                        placeholderColor = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    RegisterTextField(
                        value = confirmPasswordText,
                        onValueChange = { confirmPasswordText = it },
                        placeholder = "请再次输入密码",
                        icon = "🔒",
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                        inputColor = Color(0xFF1D150C),
                        placeholderColor = Color.Gray
                    )
                }
                2 -> {
                    Column {
                        Text("注册信息确认", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textDark)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoRow("手机号", phoneText)
                        if (nicknameText.isNotEmpty()) {
                            InfoRow("昵称", nicknameText)
                        }
                    }
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val canProceed = when (currentStep) {
                0 -> phoneText.length == 11 && codeText.length >= 4
                1 -> passwordText.length in 6..20 && confirmPasswordText == passwordText
                2 -> isAgreed
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
                            if (currentStep < 2) {
                                currentStep++
                            } else {
                                onRegister(phoneText, passwordText, nicknameText, codeText)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(if (currentStep < 2) "下一步" else "完成注册", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            if (currentStep == 2) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isAgreed) brandOrange else Color.White)
                            .border(1.dp, if (isAgreed) brandOrange else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .clickable { isAgreed = !isAgreed },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAgreed) Text("✓", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("我已阅读并同意《用户协议》和《隐私政策》", fontSize = 11.sp, color = textDark.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text("已有账号？", fontSize = 14.sp, color = textDark.copy(alpha = 0.6f))
                Text("立即登录", fontSize = 14.sp, color = brandOrange, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onLoginClick() })
            }
        }
    }
}

@Composable
private fun StepDot(step: Int, isActive: Boolean, activeColor: Color) {
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
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null,
    inputColor: Color = Color(0xFF1D150C),
    placeholderColor: Color = Color.Gray
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
        Icon(
            imageVector = when (icon) {
                "📱" -> Icons.Filled.Phone
                "🔒" -> Icons.Filled.Lock
                "👤" -> Icons.Filled.Person
                else -> Icons.Filled.Person
            },
            contentDescription = null,
            tint = textDark.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = placeholderColor, fontSize = 15.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = inputColor, fontSize = 15.sp),
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None
            )
        }
        if (isPassword) {
            IconButton(onClick = onPasswordToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                    tint = textDark.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (trailing != null) {
            trailing()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
