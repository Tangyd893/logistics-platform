package com.logistics.system.controller;

import com.logistics.common.dto.Result;
import com.logistics.system.domain.dto.MenuCreateRequest;
import com.logistics.system.domain.entity.SysMenu;
import com.logistics.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menus")
@Tag(name = "菜单管理")
@PreAuthorize("hasRole('ADMIN')")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    @GetMapping
    @Operation(summary = "菜单列表")
    public Result<List<SysMenu>> list() {
        return Result.ok(menuService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "菜单详情")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.ok(menuService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建菜单")
    public Result<SysMenu> create(@Valid @RequestBody MenuCreateRequest request) {
        return Result.ok(menuService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单")
    public Result<SysMenu> update(@PathVariable Long id, @Valid @RequestBody MenuCreateRequest request) {
        return Result.ok(menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    public Result<?> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.ok(null);
    }
}
