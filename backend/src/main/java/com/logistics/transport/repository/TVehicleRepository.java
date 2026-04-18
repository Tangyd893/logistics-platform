package com.logistics.transport.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.transport.domain.entity.TVehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TVehicleRepository extends BaseMapper<TVehicle> {
}
