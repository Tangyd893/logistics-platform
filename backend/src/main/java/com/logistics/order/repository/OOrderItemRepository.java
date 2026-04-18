package com.logistics.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.order.domain.entity.OOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OOrderItemRepository extends BaseMapper<OOrderItem> {
}
