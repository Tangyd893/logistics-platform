package com.logistics.system.controller;

import com.logistics.common.dto.Result;
import com.logistics.system.domain.dto.RoleCreateRequest;
import com.logistics.system.domain.entity.SysRole;
import com.logistics.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/roles")
@Tag(name = "角色管理")
@PreAuthorize("hasRole('ADMIN')")
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;

    @GetMapping
    @Operation(summary = "角色列表（分页）")
    public Result<?> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(roleService.page(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "角色详情")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public Result<SysRole> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Result<SysRole> update(@PathVariable Long id, @Valid @RequestBody RoleCreateRequest request) {
        return Result.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<?> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok(null);
    }
}
