package com.fooddelivery.controller;

import com.fooddelivery.dto.UserLoginDTO;
import com.fooddelivery.dto.UserRegisterDTO;
import com.fooddelivery.entity.User;
import com.fooddelivery.service.UserService;
import com.fooddelivery.utils.JwtUtil;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 */
@Slf4j
@Tag(name = "用户接口", description = "用户相关操作接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "发送验证码")
    @PostMapping("/send-code")
    public Result<String> sendCode(
            @Parameter(description = "手机号", required = true) @RequestParam @NotBlank String phone) {
        userService.sendVerifyCode(phone);
        return Result.success("验证码发送成功");
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid UserLoginDTO dto) {
        Map<String, Object> result = userService.login(dto);

        return Result.success("登录成功", result);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public Result<String> updateUser(@RequestHeader("Authorization") String token,
                                     @RequestBody User user) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        user.setId(userId);
        userService.updateUser(user);
        return Result.success("更新成功");
    }

    @Operation(summary = "重置密码（通过手机验证码）")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody com.fooddelivery.dto.UserResetPasswordDTO dto) {
        userService.resetPassword(dto.getPhone(), dto.getCode(), dto.getNewPassword());
        return Result.success("密码重置成功");
    }

    @Operation(summary = "发放优惠券给所有用户（测试用）")
    @PostMapping("/distribute-coupons")
    public Result<String> distributeCouponsToAllUsers() {
        userService.distributeCouponsToAllUsers();
        return Result.success("优惠券发放成功");
    }
}
