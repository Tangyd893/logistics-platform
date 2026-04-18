package com.logistics.system.controller;

import com.logistics.common.dto.PageDTO;
import com.logistics.common.dto.Result;
import com.logistics.system.domain.dto.UserCreateRequest;
import com.logistics.system.domain.vo.UserVO;
import com.logistics.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/users")
@Tag(name = "用户管理")
@PreAuthorize("hasRole('ADMIN')")
public class SysUserController {

    private final SysUserService userService;

    @Autowired
    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "用户列表（分页）")
    public Result<PageDTO<UserVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.page(keyword, deptId, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserCreateRequest request) {
        return Result.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id,
                                       @RequestParam(defaultValue = "123456") String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.ok();
    }
}
