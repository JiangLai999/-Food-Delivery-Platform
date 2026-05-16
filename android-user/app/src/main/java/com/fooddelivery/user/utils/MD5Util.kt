package com.fooddelivery.user.utils

import java.security.MessageDigest

object MD5Util {
    fun encrypt(text: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(text.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            text
        }
    }
}
