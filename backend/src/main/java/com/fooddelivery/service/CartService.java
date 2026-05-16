package com.fooddelivery.service;

import com.fooddelivery.model.CartItem;

import java.util.List;

/**
 * 购物车服务（简易内存实现）
 */
public interface CartService {
    void addCartItem(Long userId, Long merchantId, Long foodId, Integer quantity);
    List<CartItem> listCartItems(Long userId);
    void clearCart(Long userId);
    CartItem getCartItem(Long userId, Long foodId);
}
