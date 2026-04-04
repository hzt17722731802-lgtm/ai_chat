package com.ai.controller.admin;

import com.ai.common.Result;
import com.ai.entity.User;
import com.ai.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    public Result<Page<User>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        
        Page<User> page = userService.page(pageNum, pageSize, username, status);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable String id) {
        try {
            User user = userService.getById(id);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<String> add(@RequestBody User user) {
        try {
            String message = userService.add(user);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<String> update(@RequestBody User user) {
        try {
            String message = userService.update(user);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        try {
            String message = userService.delete(id);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable String id, @RequestParam Integer status) {
        try {
            String message = userService.updateStatus(id, status);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
