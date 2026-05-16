package com.fooddelivery.repository;

import org.springframework.stereotype.Repository;

import com.fooddelivery.entity.AdminNoticeEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class AdminNoticeRepository {
    private static final List<AdminNoticeEntity> NOTICES = new ArrayList<>();
    private static final AtomicLong NEXT_ID = new AtomicLong(1);

    public List<AdminNoticeEntity> findAll(int offset, int limit) {
        int from = Math.max(0, offset);
        int to = Math.min(NOTICES.size(), offset + limit);
        if (from >= NOTICES.size()) return new ArrayList<>();
        return new ArrayList<>(NOTICES.subList(from, to));
    }

    public AdminNoticeEntity findById(Long id) {
        for (AdminNoticeEntity n : NOTICES) {
            if (n.getId() != null && n.getId().equals(id)) return n;
        }
        return null;
    }

    public AdminNoticeEntity save(AdminNoticeEntity e) {
        e.setId(NEXT_ID.getAndIncrement());
        Date now = new Date();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        NOTICES.add(e);
        return e;
    }

    public AdminNoticeEntity update(AdminNoticeEntity e) {
        AdminNoticeEntity existing = findById(e.getId());
        if (existing == null) return null;
        if (e.getTitle() != null) existing.setTitle(e.getTitle());
        if (e.getContent() != null) existing.setContent(e.getContent());
        if (e.getStatus() != null) existing.setStatus(e.getStatus());
        existing.setUpdatedAt(new Date());
        return existing;
    }

    public void delete(Long id) {
        NOTICES.removeIf(n -> n.getId() != null && n.getId().equals(id));
    }
}
