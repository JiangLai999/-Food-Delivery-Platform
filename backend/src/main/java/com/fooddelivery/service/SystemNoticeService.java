package com.fooddelivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.SystemNotice;

import java.util.List;

/**
 * 系统公告服务接口
 */
public interface SystemNoticeService {

    /**
     * 获取用户公告列表
     */
    List<SystemNotice> getUserNotices();

    /**
     * 获取商家公告列表
     */
    List<SystemNotice> getMerchantNotices();

    /**
     * 获取所有公告列表
     */
    Page<SystemNotice> getAllNotices(Integer pageNum, Integer pageSize);

    /**
     * 获取公告详情
     */
    SystemNotice getNoticeById(Long noticeId);
}
