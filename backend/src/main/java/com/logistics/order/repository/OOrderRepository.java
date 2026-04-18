package com.logistics.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.order.domain.entity.OOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OOrderRepository extends BaseMapper<OOrder> {
}
