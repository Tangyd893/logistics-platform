package com.logistics.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.dto.PageDTO;
import com.logistics.common.exception.BusinessException;
import com.logistics.system.domain.dto.RoleCreateRequest;
import com.logistics.system.domain.entity.SysRole;
import com.logistics.system.repository.SysRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysRoleService {

    @Autowired
    private SysRoleRepository roleRepository;

    public PageDTO<SysRole> page(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysRole::getName, keyword).or().like(SysRole::getCode, keyword);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByDesc(SysRole::getCreatedAt);
        Page<SysRole> result = roleRepository.selectPage(new Page<>(page, size), wrapper);
        return PageDTO.of(result.getRecords(), result.getTotal(), page, size);
    }

    public SysRole getById(Long id) {
        SysRole role = roleRepository.selectById(id);
        if (role == null) throw new BusinessException("角色不存在");
        return role;
    }

    @Transactional
    public SysRole create(RoleCreateRequest request) {
        // 检查编码唯一
        long count = roleRepository.selectCount(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, request.getCode()));
        if (count > 0) throw new BusinessException("角色编码已存在");
        SysRole role = new SysRole();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        roleRepository.insert(role);
        return role;
    }

    @Transactional
    public SysRole update(Long id, RoleCreateRequest request) {
        SysRole role = roleRepository.selectById(id);
        if (role == null) throw new BusinessException("角色不存在");
        // 检查编码唯一（排除自己）
        long count = roleRepository.selectCount(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, request.getCode())
                .ne(SysRole::getId, id));
        if (count > 0) throw new BusinessException("角色编码已存在");
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        if (request.getStatus() != null) role.setStatus(request.getStatus());
        roleRepository.updateById(role);
        return role;
    }

    @Transactional
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}
