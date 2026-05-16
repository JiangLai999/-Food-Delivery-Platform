package com.fooddelivery.controller;

import com.fooddelivery.entity.AdminNoticeEntity;
import com.fooddelivery.dto.AdminNoticeDTO;
import com.fooddelivery.repository.AdminNoticeRepository;
import com.fooddelivery.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminNoticeController {

    @Autowired
    private AdminNoticeRepository noticeRepo;

    @GetMapping("/announcements")
    public Result<List<AdminNoticeEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<AdminNoticeEntity> notices = noticeRepo.findAll(offset, pageSize);
        return Result.success(notices);
    }

    @PostMapping("/announcements")
    public Result<AdminNoticeEntity> create(@RequestBody AdminNoticeDTO dto) {
        AdminNoticeEntity e = new AdminNoticeEntity();
        e.setTitle(dto.getTitle());
        e.setContent(dto.getContent());
        e.setStatus(dto.getStatus());
        AdminNoticeEntity saved = noticeRepo.save(e);
        return Result.success(saved);
    }

    @PutMapping("/announcements/{id}")
    public Result<AdminNoticeEntity> update(@PathVariable Long id, @RequestBody AdminNoticeDTO dto) {
        AdminNoticeEntity existing = noticeRepo.findById(id);
        if (existing == null) return Result.error("Notice not found");
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getContent() != null) existing.setContent(dto.getContent());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        AdminNoticeEntity updated = noticeRepo.update(existing);
        return Result.success(updated);
    }

    @DeleteMapping("/announcements/{id}")
    public Result<String> delete(@PathVariable Long id) {
        noticeRepo.delete(id);
        return Result.success("Deleted");
    }
}
