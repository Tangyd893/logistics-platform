package com.logistics.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.system.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMenuRepository extends BaseMapper<SysMenu> {
}
