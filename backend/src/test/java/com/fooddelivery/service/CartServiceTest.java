package com.fooddelivery.service;

import com.fooddelivery.model.CartItem;
import com.fooddelivery.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简易购物车服务测试（内存实现）
 */
public class CartServiceTest {

    private final CartService cartService = new CartServiceImpl();

    @Test
    public void testAddListAndClearCart() {
        Long userId = 1L;
        cartService.addCartItem(userId, 1001L, 2001L, 2);
        List<CartItem> items = cartService.listCartItems(userId);
        assertNotNull(items);
        assertEquals(1, items.size());
        CartItem item = items.get(0);
        assertEquals(Long.valueOf(2001), item.getFoodId());
        assertEquals(Integer.valueOf(2), item.getQuantity());

        cartService.clearCart(userId);
        items = cartService.listCartItems(userId);
        assertTrue(items.isEmpty());
    }
}
