package com.logistics.warehouse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.warehouse.domain.entity.WhLocation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WhLocationRepository extends BaseMapper<WhLocation> {
}
