package com.fooddelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.service.MerchantService;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    @Operation(summary = "管理员获取商家分页列表")
    @GetMapping("/merchants-by-category")
    public Result<Page<Merchant>> getMerchants(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) Long categoryId) {
        Page<Merchant> page = merchantService.getMerchantPage(pageNum, pageSize, categoryId);
        return Result.success(page);
    }
}
