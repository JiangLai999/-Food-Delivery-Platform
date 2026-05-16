package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.entity.CartItemEntity;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.mapper.CartItemMapper;
import com.fooddelivery.mapper.FoodItemMapper;
import com.fooddelivery.model.CartItem;
import com.fooddelivery.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * 简易购物车服务实现（内存存储，示例用途，支持后续 DB 持久化扩展）
 */
@Service
public class CartServiceImpl implements CartService {

    // 内存缓存：用户ID -> 购物车条目列表（回退路径）
    private final Map<Long, List<CartItem>> cartStore = new HashMap<>();

    @Autowired(required = false)
    private CartItemMapper cartItemMapper;

    @Autowired(required = false)
    private FoodItemMapper foodItemMapper;

    @Value("${cart.persistence:memory}")
    private String cartPersistence;

    private boolean useDb() {
        return "db".equalsIgnoreCase(cartPersistence);
    }

    @Override
    public void addCartItem(Long userId, Long merchantId, Long foodId, Integer quantity) {
        if (useDb() && cartItemMapper != null) {
            // DB 持久化路径
            LambdaQueryWrapper<CartItemEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CartItemEntity::getUserId, userId)
                   .eq(CartItemEntity::getFoodId, foodId);
            CartItemEntity existing = cartItemMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + quantity);
                cartItemMapper.updateById(existing);
            } else {
                FoodItem food = (foodItemMapper != null) ? foodItemMapper.selectById(foodId) : null;
                CartItemEntity entity = new CartItemEntity();
                entity.setUserId(userId);
                entity.setMerchantId(merchantId);
                entity.setFoodId(foodId);
                entity.setQuantity(quantity);
                if (food != null) {
                    entity.setFoodName(food.getFoodName());
                    entity.setFoodImage(food.getImage());
                    entity.setPrice(food.getPrice());
                } else {
                    entity.setPrice(BigDecimal.ZERO);
                }
                cartItemMapper.insert(entity);
            }
            return;
        }

        // 内存路径
        List<CartItem> items = cartStore.computeIfAbsent(userId, k -> new ArrayList<>());
        for (CartItem item : items) {
            if (item.getFoodId().equals(foodId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        CartItem newItem = new CartItem();
        newItem.setFoodId(foodId);
        newItem.setMerchantId(merchantId);
        newItem.setQuantity(quantity);
        newItem.setFoodName(null);
        newItem.setFoodImage(null);
        newItem.setPrice(BigDecimal.ZERO);
        items.add(newItem);
    }

    @Override
    public List<CartItem> listCartItems(Long userId) {
        if (useDb() && cartItemMapper != null) {
            List<CartItemEntity> entities = cartItemMapper.selectList(
                    new LambdaQueryWrapper<CartItemEntity>().eq(CartItemEntity::getUserId, userId)
            );
            List<CartItem> result = new ArrayList<>();
            if (entities != null) {
                for (CartItemEntity e : entities) {
                    CartItem ci = new CartItem();
                    ci.setFoodId(e.getFoodId());
                    ci.setMerchantId(e.getMerchantId());
                    ci.setFoodName(e.getFoodName());
                    ci.setFoodImage(e.getFoodImage());
                    ci.setPrice(e.getPrice());
                    ci.setQuantity(e.getQuantity());
                    result.add(ci);
                }
            }
            return result;
        }

        return cartStore.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public void clearCart(Long userId) {
        if (useDb() && cartItemMapper != null) {
            cartItemMapper.delete(new LambdaQueryWrapper<CartItemEntity>().eq(CartItemEntity::getUserId, userId));
        } else {
            cartStore.remove(userId);
        }
    }

    @Override
    public CartItem getCartItem(Long userId, Long foodId) {
        if (useDb() && cartItemMapper != null) {
            CartItemEntity entity = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItemEntity>()
                    .eq(CartItemEntity::getUserId, userId)
                    .eq(CartItemEntity::getFoodId, foodId));
            if (entity == null) return null;
            CartItem ci = new CartItem();
            ci.setFoodId(entity.getFoodId());
            ci.setMerchantId(entity.getMerchantId());
            ci.setFoodName(entity.getFoodName());
            ci.setFoodImage(entity.getFoodImage());
            ci.setPrice(entity.getPrice());
            ci.setQuantity(entity.getQuantity());
            return ci;
        }

        List<CartItem> items = cartStore.get(userId);
        if (items == null) return null;
        for (CartItem item : items) {
            if (item.getFoodId().equals(foodId)) return item;
        }
        return null;
    }
}
