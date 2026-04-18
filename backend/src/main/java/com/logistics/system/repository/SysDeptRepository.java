package com.logistics.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.system.domain.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDeptRepository extends BaseMapper<SysDept> {

    List<SysDept> findByParentIdOrderBySortOrder(Long parentId);

    List<SysDept> findAllByOrderBySortOrder();
}
