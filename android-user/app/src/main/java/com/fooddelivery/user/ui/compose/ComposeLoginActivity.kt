package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.ui.compose.screens.LoginScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeLoginActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        startActivity(Intent(this@ComposeLoginActivity, ComposeMainActivity::class.java))
                        finish()
                    }
                }
                
                LoginScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onClearError = {
                        viewModel.clearError()
                    },
                    onLogin = { phone, password, loginType ->
                        viewModel.clearError()
                        viewModel.login(
                            phone = phone,
                            password = password,
                            loginType = loginType,
                            onSuccess = {
                                Toast.makeText(this@ComposeLoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(this@ComposeLoginActivity, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onSendCode = { phone, onSuccess, onError ->
                        viewModel.clearError()
                        viewModel.sendVerifyCode(phone, onSuccess, onError)
                    },
                    onRegister = { phone, password, nickname ->
                        startActivity(Intent(this@ComposeLoginActivity, ComposeRegisterActivity::class.java))
                    },
                    onForgotPassword = {
                        startActivity(Intent(this@ComposeLoginActivity, ComposeForgotPasswordActivity::class.java))
                    }
                )
            }
        }
    }
}
