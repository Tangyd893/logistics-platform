package com.logistics.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.system.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface SysUserRepository extends BaseMapper<SysUser> {

    Optional<SysUser> findByUsername(String username);

    Optional<SysUser> findByPhone(String phone);

    boolean existsByUsername(String username);
}
