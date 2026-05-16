package com.fooddelivery.user.ui.compose.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fooddelivery.user.utils.MD5Util
import com.fooddelivery.user.ui.compose.theme.*

@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onLogin: (String, String, Int) -> Unit = { _, _, _ -> },
    onSendCode: (String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onClearError: () -> Unit = {},
    onRegister: (String, String, String) -> Unit = { _, _, _ -> },
    onForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val brandOrange = Color(0xFFFF8C00)
    val textDark = Color(0xFF1D150C)
    var isCodeLogin by remember { mutableStateOf(true) }
    var phoneText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var codeText by remember { mutableStateOf("") }
    var nicknameText by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    
    // 尝试加载商标图标
    val logoBitmap = remember {
        try {
            val drawable = context.getDrawable(com.fooddelivery.user.R.drawable.ic_launcher)
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
            // 深色背景 + 橙色渐变
            .background(Color(0xFF1A1A2E))
    ) {
        // 背景装饰 - 橙色光晕
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
        
        // 顶部装饰圆形
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .size(200.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0x30FF8C00), Color(0x00FF8C00))
                    )
                )
        )
        
        // 底部装饰圆形
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .size(180.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0x25FF8C00), Color(0x00FF8C00))
                    )
                )
        )

        // 登录卡片 - 毛玻璃效果
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 商标Logo
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
                
                // 标题
                Text("欢迎回来", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("登录您的账号，发现身边美食", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(28.dp))

                // 登录方式切换
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCodeLogin) brandOrange else Color.Transparent)
                            .clickable { isCodeLogin = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("验证码登录", color = if (isCodeLogin) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isCodeLogin) brandOrange else Color.Transparent)
                            .clickable { isCodeLogin = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("密码登录", color = if (!isCodeLogin) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 手机号输入框
                LoginTextField(
                    value = phoneText,
                    onValueChange = { phoneText = it.filter { c -> c.isDigit() }.take(11) },
                    placeholder = "请输入手机号",
                    icon = "📱",
                    inputColor = Color.White,
                    placeholderColor = Color.White.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (isCodeLogin) {
                    LoginTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        placeholder = "请输入验证码",
                        icon = "🔒",
                        inputColor = Color.White,
                        placeholderColor = Color.White.copy(alpha = 0.3f),
                        trailing = {
                            Text(
                                if (countdown > 0) "${countdown}s" else "获取验证码", 
                                color = if (countdown > 0) Color.White else brandOrange, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier
                                    .background(
                                        if (countdown == 0 && phoneText.length == 11) Color.White.copy(alpha = 0.2f) else Color.Transparent, 
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable { 
                                        when {
                                            phoneText.length != 11 -> {
                                                android.widget.Toast.makeText(context, "请输入11位手机号", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            countdown > 0 -> {}
                                            else -> {
                                                onSendCode(phoneText, { 
                                                    countdown = 60
                                                    android.widget.Toast.makeText(context, "验证码: 123456", android.widget.Toast.LENGTH_LONG).show()
                                                }, { error -> 
                                                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                                                })
                                            }
                                        }
                                    }
                            )
                        }
                    )
                } else {
                    LoginTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        placeholder = "请输入密码",
                        icon = "🔒",
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        inputColor = Color.White,
                        placeholderColor = Color.White.copy(alpha = 0.3f)
                    )
                }

                // 忘记密码
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    Text("忘记密码？", fontSize = 12.sp, color = brandOrange, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onForgotPassword() })
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 登录按钮
                val isButtonEnabled = !isLoading && isAgreed && phoneText.isNotBlank() &&
                    (if (isCodeLogin) codeText.isNotBlank() else passwordText.isNotBlank())

                val buttonModifier = if (isButtonEnabled) {
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(brandOrange, Color(0xFFFFAA33))))
                        .clickable {
                            val password = if (isCodeLogin) codeText else passwordText
                            val loginType = if (isCodeLogin) 1 else 0
                            onLogin(phoneText, password, loginType)
                        }
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brandOrange.copy(alpha = 0.4f))
                        .clickable { }
                }

                Box(
                    modifier = buttonModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text("登 录", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(error, color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 用户协议
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isAgreed) brandOrange else Color.White.copy(alpha = 0.1f))
                            .border(1.dp, if (isAgreed) brandOrange else Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .clickable { isAgreed = !isAgreed },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAgreed) Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("我已阅读并同意 《用户协议》 与 《隐私政策》", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), lineHeight = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 注册入口
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("还没有账号？", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                    Text(" 立即注册", fontSize = 14.sp, color = brandOrange, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onRegister(phoneText, passwordText, "") })
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null,
    inputColor: Color = Color(0xFF1D150C),
    placeholderColor: Color = Color(0xFF1D150C).copy(alpha = 0.3f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (icon) {
                "📱" -> Icons.Filled.Smartphone
                "🔒" -> Icons.Filled.Lock
                else -> Icons.Filled.Smartphone
            },
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
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
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (trailing != null) {
            trailing()
        }
    }
}

@Composable
private fun ThirdPartyLoginBtn(label: String, hoverColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.4f), CircleShape)
                .border(1.dp, Color(0xFFFF8C00).copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(label.take(1), fontWeight = FontWeight.Bold, color = Color(0xFF1D150C).copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 10.sp, color = Color(0xFF1D150C).copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
    }
}
