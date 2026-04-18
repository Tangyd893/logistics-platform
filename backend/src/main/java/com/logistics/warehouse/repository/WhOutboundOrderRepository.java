package com.logistics.warehouse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.warehouse.domain.entity.WhOutboundOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WhOutboundOrderRepository extends BaseMapper<WhOutboundOrder> {
}
