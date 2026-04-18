package com.logistics.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.system.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleRepository extends BaseMapper<SysRole> {
}
