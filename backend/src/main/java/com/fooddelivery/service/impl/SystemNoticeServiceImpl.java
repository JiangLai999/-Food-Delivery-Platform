package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fooddelivery.entity.SystemNotice;
import com.fooddelivery.mapper.SystemNoticeMapper;
import com.fooddelivery.service.SystemNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统公告服务实现类
 */
@Slf4j
@Service
public class SystemNoticeServiceImpl implements SystemNoticeService {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public List<SystemNotice> getUserNotices() {
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotice::getStatus, 1) // 已发布
                .in(SystemNotice::getTargetType, 0, 1) // 全部或用户
                .orderByDesc(SystemNotice::getPublishTime)
                .last("LIMIT 20");
        return systemNoticeMapper.selectList(wrapper);
    }

    @Override
    public List<SystemNotice> getMerchantNotices() {
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotice::getStatus, 1) // 已发布
                .in(SystemNotice::getTargetType, 0, 2) // 全部或商家
                .orderByDesc(SystemNotice::getPublishTime)
                .last("LIMIT 20");
        return systemNoticeMapper.selectList(wrapper);
    }

    @Override
    public Page<SystemNotice> getAllNotices(Integer pageNum, Integer pageSize) {
        Page<SystemNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotice::getStatus, 1)
                .orderByDesc(SystemNotice::getPublishTime);
        return systemNoticeMapper.selectPage(page, wrapper);
    }

    @Override
    public SystemNotice getNoticeById(Long noticeId) {
        return systemNoticeMapper.selectById(noticeId);
    }
}
