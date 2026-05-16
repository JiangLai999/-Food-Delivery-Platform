package com.fooddelivery.user.network

import com.google.gson.annotations.SerializedName

/**
 * 用户重置密码请求
 */
data class UserResetPasswordRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("code")
    val code: String,
    
    @SerializedName("newPassword")
    val newPassword: String
)
