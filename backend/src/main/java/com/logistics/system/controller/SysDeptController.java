package com.logistics.system.controller;

import com.logistics.common.dto.Result;
import com.logistics.system.domain.dto.DeptCreateRequest;
import com.logistics.system.domain.entity.SysDept;
import com.logistics.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/depts")
@Tag(name = "部门管理")
@PreAuthorize("hasRole('ADMIN')")
public class SysDeptController {

    @Autowired
    private SysDeptService deptService;

    @GetMapping
    @Operation(summary = "部门列表")
    public Result<List<SysDept>> list() {
        return Result.ok(deptService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "部门详情")
    public Result<SysDept> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建部门")
    public Result<SysDept> create(@Valid @RequestBody DeptCreateRequest request) {
        return Result.ok(deptService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    public Result<SysDept> update(@PathVariable Long id, @Valid @RequestBody DeptCreateRequest request) {
        return Result.ok(deptService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    public Result<?> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok(null);
    }
}
