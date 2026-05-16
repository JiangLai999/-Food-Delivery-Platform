package com.fooddelivery.controller;

import com.fooddelivery.entity.User;
import com.fooddelivery.service.UserService;
import com.fooddelivery.vo.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminUserController {
    @Autowired
    private UserService userService;

    @GetMapping("/users-simple")
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> page = userService.getUsersPage(pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/users/{userId}/ban")
    public Result<String> banUser(@PathVariable Long userId) {
        userService.banUser(userId);
        return Result.success("User banned");
    }

    @PostMapping("/users/{userId}/unban")
    public Result<String> unbanUser(@PathVariable Long userId) {
        userService.unbanUser(userId);
        return Result.success("User unbanned");
    }
}
