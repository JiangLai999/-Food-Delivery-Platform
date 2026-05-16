package com.fooddelivery.controller;

import com.fooddelivery.dto.CartAddDTO;
import com.fooddelivery.dto.CartCheckoutDTO;
import com.fooddelivery.model.CartItem;
import com.fooddelivery.service.CartService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 简易购物车 Controller（内存实现）
 */
@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<String> addToCart(@RequestHeader("Authorization") String token,
                                    @Valid @RequestBody CartAddDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        cartService.addCartItem(userId, dto.getMerchantId(), dto.getFoodId(), dto.getQuantity());
        return Result.success("添加到购物车成功");
    }

    @GetMapping("/list")
    public Result<List<CartItem>> listCart(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<CartItem> items = cartService.listCartItems(userId);
        return Result.success(items);
    }

    @PostMapping("/checkout")
    public Result<CartCheckoutDTO> checkout(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<CartItem> items = cartService.listCartItems(userId);
        CartCheckoutDTO checkout = new CartCheckoutDTO();
        checkout.setItems(items);
        // 价格汇总：若购物车条目中价格为0，需要前端通过商品查询填充价格，示例中直接累计价格为0
        checkout.setTotalAmount(java.math.BigDecimal.ZERO);
        return Result.success(checkout);
    }

    @PostMapping("/clear")
    public Result<String> clearCart(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        cartService.clearCart(userId);
        return Result.success("购物车已清空");
    }
}
