package com.fooddelivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.SystemNotice;
import com.fooddelivery.service.SystemNoticeService;
import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统公告Controller
 */
@Slf4j
@Tag(name = "系统公告接口", description = "系统公告管理接口")
@RestController
@RequestMapping("/notice")
public class SystemNoticeController {

    @Autowired
    private SystemNoticeService systemNoticeService;

    @Operation(summary = "获取用户公告列表")
    @GetMapping("/user")
    public Result<List<SystemNotice>> getUserNotices() {
        List<SystemNotice> notices = systemNoticeService.getUserNotices();
        return Result.success(notices);
    }

    @Operation(summary = "获取商家公告列表")
    @GetMapping("/merchant")
    public Result<List<SystemNotice>> getMerchantNotices() {
        List<SystemNotice> notices = systemNoticeService.getMerchantNotices();
        return Result.success(notices);
    }

    @Operation(summary = "获取所有公告列表")
    @GetMapping("/all")
    public Result<Page<SystemNotice>> getAllNotices(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SystemNotice> page = systemNoticeService.getAllNotices(pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取公告详情")
    @GetMapping("/{noticeId}")
    public Result<SystemNotice> getNoticeById(
            @Parameter(description = "公告ID") @PathVariable Long noticeId) {
        SystemNotice notice = systemNoticeService.getNoticeById(noticeId);
        return Result.success(notice);
    }
}