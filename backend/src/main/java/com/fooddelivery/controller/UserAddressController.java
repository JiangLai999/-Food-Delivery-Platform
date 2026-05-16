package com.fooddelivery.controller;

import com.fooddelivery.entity.UserAddress;
import com.fooddelivery.service.UserAddressService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户地址Controller
 */
@Slf4j
@Tag(name = "用户地址接口", description = "用户地址管理接口")
@RestController
@RequestMapping("/user/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "添加地址")
    @PostMapping("/add")
    public Result<String> addAddress(@RequestHeader("Authorization") String token,
                                     @RequestBody @Valid UserAddress address) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        address.setUserId(userId);
        userAddressService.addAddress(address);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新地址")
    @PutMapping("/update")
    public Result<String> updateAddress(@RequestHeader("Authorization") String token,
                                        @RequestBody @Valid UserAddress address) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        address.setUserId(userId);
        userAddressService.updateAddress(address);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/delete/{addressId}")
    public Result<String> deleteAddress(@RequestHeader("Authorization") String token,
                                        @Parameter(description = "地址ID") @PathVariable Long addressId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        userAddressService.deleteAddress(addressId, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取用户所有地址")
    @GetMapping("/list")
    public Result<List<UserAddress>> getUserAddresses(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<UserAddress> addresses = userAddressService.getUserAddresses(userId);
        return Result.success(addresses);
    }

    @Operation(summary = "获取默认地址")
    @GetMapping("/default")
    public Result<UserAddress> getDefaultAddress(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserAddress address = userAddressService.getDefaultAddress(userId);
        return Result.success(address);
    }

    @Operation(summary = "设置默认地址")
    @PutMapping("/default/{addressId}")
    public Result<String> setDefaultAddress(@RequestHeader("Authorization") String token,
                                            @Parameter(description = "地址ID") @PathVariable Long addressId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        userAddressService.setDefaultAddress(addressId, userId);
        return Result.success("设置成功");
    }
}