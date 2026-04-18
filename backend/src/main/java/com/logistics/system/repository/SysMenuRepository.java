package com.logistics.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.system.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMenuRepository extends BaseMapper<SysMenu> {

    List<SysMenu> findByParentIdOrderBySortOrder(Long parentId);

    List<SysMenu> findByTypeOrderBySortOrder(Integer type);
}
