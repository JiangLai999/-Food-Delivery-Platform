package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.ui.compose.screens.RegisterScreen
import com.fooddelivery.user.ui.compose.screens.ForgotPasswordScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeRegisterActivity : ComponentActivity() {
    
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val context = LocalContext.current
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
                
                RegisterScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onClearError = {
                        viewModel.clearError()
                    },
                    onSendCode = { phone, onSuccess, onError ->
                        viewModel.clearError()
                        viewModel.sendVerifyCode(
                            phone = phone,
                            onSuccess = {
                                onSuccess()
                                Toast.makeText(context, "验证码已发送", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                onError(error)
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onRegister = { phone, password, nickname, code ->
                        viewModel.clearError()
                        viewModel.register(
                            phone = phone,
                            password = password,
                            nickname = nickname,
                            code = code,
                            onSuccess = {
                                Toast.makeText(context, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                                finish()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onLoginClick = {
                        finish()
                    },
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }
}

class ComposeForgotPasswordActivity : ComponentActivity() {
    
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val context = LocalContext.current
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
                var successMessage by remember { mutableStateOf<String?>(null) }
                
                ForgotPasswordScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    successMessage = successMessage,
                    onClearError = {
                        viewModel.clearError()
                        successMessage = null
                    },
                    onSendCode = { phone, onSuccess, onError ->
                        viewModel.clearError()
                        viewModel.sendVerifyCode(
                            phone = phone,
                            onSuccess = {
                                onSuccess()
                                android.util.Log.d("ForgotPasswordActivity", "验证码已发送")
                                Toast.makeText(context, "验证码已发送", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                android.util.Log.e("ForgotPasswordActivity", "发送验证码失败: $error")
                                Toast.makeText(context, "❌ $error", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onResetPassword = { phone, code, password ->
                        viewModel.clearError()
                        android.util.Log.d("ForgotPasswordActivity", "开始重置密码")
                        android.util.Log.d("ForgotPasswordActivity", "手机号: $phone")
                        android.util.Log.d("ForgotPasswordActivity", "验证码: $code")
                        android.util.Log.d("ForgotPasswordActivity", "密码长度: ${password.length}")
                        
                        // 密码由后端进行MD5加密，前端传递明文
                        viewModel.resetPassword(
                            phone = phone,
                            code = code,
                            newPassword = password,
                            onSuccess = {
                                android.util.Log.d("ForgotPasswordActivity", "密码重置成功")
                                successMessage = "密码重置成功"
                                Toast.makeText(context, "✅ 密码重置成功！", Toast.LENGTH_SHORT).show()
                                
                                // 延迟返回登录
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    Toast.makeText(context, "正在返回登录页面...", Toast.LENGTH_SHORT).show()
                                    finish()
                                }, 1000)
                            },
                            onError = { error ->
                                android.util.Log.e("ForgotPasswordActivity", "重置密码失败: $error")
                                Toast.makeText(context, "❌ 重置密码失败", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }
}
