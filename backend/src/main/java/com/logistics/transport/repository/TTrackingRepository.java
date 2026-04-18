package com.logistics.transport.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.transport.domain.entity.TTracking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TTrackingRepository extends BaseMapper<TTracking> {
}
