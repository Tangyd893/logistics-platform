package com.logistics.transport.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.transport.domain.entity.TDriver;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TDriverRepository extends BaseMapper<TDriver> {
}
