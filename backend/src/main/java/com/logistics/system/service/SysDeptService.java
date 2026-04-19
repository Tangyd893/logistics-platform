package com.logistics.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.common.dto.PageDTO;
import com.logistics.common.exception.BusinessException;
import com.logistics.system.domain.dto.DeptCreateRequest;
import com.logistics.system.domain.entity.SysDept;
import com.logistics.system.repository.SysDeptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDeptService {

    @Autowired
    private SysDeptRepository deptRepository;

    public List<SysDept> list() {
        return deptRepository.selectList(
            new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSortOrder)
                .orderByAsc(SysDept::getCreatedAt));
    }

    public SysDept getById(Long id) {
        SysDept dept = deptRepository.selectById(id);
        if (dept == null) throw new BusinessException("部门不存在");
        return dept;
    }

    @Transactional
    public SysDept create(DeptCreateRequest request) {
        SysDept dept = new SysDept();
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        dept.setName(request.getName());
        dept.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        deptRepository.insert(dept);
        return dept;
    }

    @Transactional
    public SysDept update(Long id, DeptCreateRequest request) {
        SysDept dept = deptRepository.selectById(id);
        if (dept == null) throw new BusinessException("部门不存在");
        dept.setName(request.getName());
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        dept.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        deptRepository.updateById(dept);
        return dept;
    }

    @Transactional
    public void delete(Long id) {
        // 检查是否有子部门
        long childCount = deptRepository.selectCount(
            new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount > 0) throw new BusinessException("请先删除子部门");
        deptRepository.deleteById(id);
    }
}
