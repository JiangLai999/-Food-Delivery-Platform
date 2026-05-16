package com.fooddelivery.user.ui.compose.manager

import com.fooddelivery.user.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

object CartManager {
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    private val _currentMerchantId = MutableStateFlow<Long?>(null)
    val currentMerchantId: StateFlow<Long?> = _currentMerchantId.asStateFlow()
    
    private val _currentMerchantName = MutableStateFlow<String?>(null)
    val currentMerchantName: StateFlow<String?> = _currentMerchantName.asStateFlow()
    
    private val _deliveryFee = MutableStateFlow(BigDecimal.ZERO)
    val deliveryFee: StateFlow<BigDecimal> = _deliveryFee.asStateFlow()
    
    private val _minOrderAmount = MutableStateFlow(BigDecimal.ZERO)
    val minOrderAmount: StateFlow<BigDecimal> = _minOrderAmount.asStateFlow()

    fun setMerchantInfo(merchantId: Long, merchantName: String, deliveryFee: BigDecimal, minOrderAmount: BigDecimal) {
        if (_currentMerchantId.value != null && _currentMerchantId.value != merchantId && _cartItems.value.isNotEmpty()) {
            clearCart()
        }
        _currentMerchantId.value = merchantId
        _currentMerchantName.value = merchantName
        _deliveryFee.value = deliveryFee
        _minOrderAmount.value = minOrderAmount
    }

    fun addItem(foodId: Long, foodName: String, foodImage: String?, price: BigDecimal, quantity: Int = 1) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.foodId == foodId }
        
        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = CartItem(
                foodId,
                foodName,
                foodImage,
                price,
                existing.quantity + quantity
            )
        } else {
            currentItems.add(CartItem(foodId, foodName, foodImage, price, quantity))
        }
        
        _cartItems.value = currentItems
    }

    fun updateQuantity(foodId: Long, quantity: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.foodId == foodId }
        
        if (index >= 0) {
            if (quantity <= 0) {
                currentItems.removeAt(index)
            } else {
                val existing = currentItems[index]
                currentItems[index] = CartItem(foodId, existing.foodName, existing.foodImage, existing.price, quantity)
            }
            _cartItems.value = currentItems
        }
    }

    fun removeItem(foodId: Long) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.foodId == foodId }
        _cartItems.value = currentItems
    }

    fun increaseQuantity(foodId: Long) {
        val currentItems = _cartItems.value
        val item = currentItems.find { it.foodId == foodId }
        if (item != null) {
            updateQuantity(foodId, item.quantity + 1)
        }
    }

    fun decreaseQuantity(foodId: Long) {
        val currentItems = _cartItems.value
        val item = currentItems.find { it.foodId == foodId }
        if (item != null) {
            updateQuantity(foodId, item.quantity - 1)
        }
    }

    fun getItemQuantity(foodId: Long): Int {
        return _cartItems.value.find { it.foodId == foodId }?.quantity ?: 0
    }

    fun getTotalAmount(): BigDecimal {
        return _cartItems.value.fold(BigDecimal.ZERO) { total, item ->
            total.add(item.getSubtotal())
        }
    }

    fun getFinalAmount(): BigDecimal {
        return getTotalAmount().add(_deliveryFee.value)
    }

    fun getItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun isMinOrderMet(): Boolean {
        return getTotalAmount() >= _minOrderAmount.value
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _currentMerchantId.value = null
        _currentMerchantName.value = null
        _deliveryFee.value = BigDecimal.ZERO
        _minOrderAmount.value = BigDecimal.ZERO
    }

    fun isEmpty(): Boolean {
        return _cartItems.value.isEmpty()
    }
}
