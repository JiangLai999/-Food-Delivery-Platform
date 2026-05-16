package com.fooddelivery.controller;

import com.fooddelivery.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/admin")
public class AdminCategoryController {

    private static final List<Category> CATS = new ArrayList<>();
    private static final AtomicLong NEXT_ID = new AtomicLong(1);

    private static class Category {
        Long id;
        String name;
    }

    @GetMapping("/categories/list")
    public Result<List<Category>> list(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, CATS.size());
        if (from < 0 || from >= CATS.size()) return Result.success(new ArrayList<>());
        return Result.success(CATS.subList(from, to));
    }

    @PostMapping("/categories/create")
    public Result<Category> create(@RequestBody Category payload) {
        Category c = new Category();
        c.id = NEXT_ID.getAndIncrement();
        c.name = payload.name;
        CATS.add(c);
        return Result.success(c);
    }

    @PutMapping("/categories/update/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category payload) {
        for (Category c : CATS) {
            if (c.id.equals(id)) {
                c.name = payload.name;
                return Result.success(c);
            }
        }
        return Result.error("Not found");
    }

    @DeleteMapping("/categories/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        CATS.removeIf(c -> c.id.equals(id));
        return Result.success("Deleted");
    }
}
