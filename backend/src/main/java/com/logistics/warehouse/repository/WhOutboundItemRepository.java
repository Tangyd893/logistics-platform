package com.logistics.warehouse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.warehouse.domain.entity.WhOutboundItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WhOutboundItemRepository extends BaseMapper<WhOutboundItem> {
}
