package com.logistics.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.exception.BusinessException;
import com.logistics.order.domain.dto.OrderCreateRequest;
import com.logistics.order.domain.entity.OOrder;
import com.logistics.order.domain.entity.OOrderItem;
import com.logistics.order.domain.entity.OOrderStatusLog;
import com.logistics.order.domain.vo.OrderVO;
import com.logistics.order.domain.vo.OrderVO.OrderItemVO;
import com.logistics.order.domain.vo.OrderVO.StatusLogVO;
import com.logistics.order.repository.OOrderItemRepository;
import com.logistics.order.repository.OOrderRepository;
import com.logistics.order.repository.OOrderStatusLogRepository;
import com.logistics.system.domain.entity.SysUser;
import com.logistics.system.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OOrderRepository orderRepository;
    private final OOrderItemRepository orderItemRepository;
    private final OOrderStatusLogRepository statusLogRepository;
    private final SysUserRepository userRepository;

    @Autowired
    public OrderService(OOrderRepository orderRepository,
                        OOrderItemRepository orderItemRepository,
                        OOrderStatusLogRepository statusLogRepository,
                        SysUserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusLogRepository = statusLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * 分页查询订单
     */
    public com.logistics.common.dto.PageDTO<OrderVO> page(
            Long customerId, String keyword, Integer status,
            String senderName, String receiverName,
            int page, int size) {

        LambdaQueryWrapper<OOrder> wrapper = new LambdaQueryWrapper<>();
        if (customerId != null) {
            wrapper.eq(OOrder::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(OOrder::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(OOrder::getOrderNo, keyword)
                    .or().like(OOrder::getSenderName, keyword)
                    .or().like(OOrder::getReceiverName, keyword));
        }
        if (senderName != null && !senderName.isBlank()) {
            wrapper.like(OOrder::getSenderName, senderName);
        }
        if (receiverName != null && !receiverName.isBlank()) {
            wrapper.like(OOrder::getReceiverName, receiverName);
        }
        wrapper.orderByDesc(OOrder::getCreatedAt);

        Page<OOrder> result = orderRepository.selectPage(new Page<>(page, size), wrapper);
        List<OrderVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return com.logistics.common.dto.PageDTO.of(voList, result.getTotal(), page, size);
    }

    /**
     * 根据ID查询订单
     */
    public OrderVO getById(Long id) {
        OOrder order = orderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(order);
    }

    /**
     * 根据订单号查询
     */
    public OrderVO getByOrderNo(String orderNo) {
        LambdaQueryWrapper<OOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OOrder::getOrderNo, orderNo);
        OOrder order = orderRepository.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(order);
    }

    /**
     * 创建订单
     */
    @Transactional
    public OrderVO create(OrderCreateRequest request) {
        String orderNo = generateOrderNo();

        OOrder order = new OOrder();
        order.setOrderNo(orderNo);
        order.setCustomerId(request.getCustomerId());
        order.setSenderName(request.getSenderName());
        order.setSenderPhone(request.getSenderPhone());
        order.setSenderAddress(request.getSenderAddress());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setTotalAmount(request.getTotalAmount());
        order.setWeightKg(request.getWeightKg());
        order.setVolumeCbm(request.getVolumeCbm());
        order.setStatus(10); // 待确认
        order.setRemark(request.getRemark());

        orderRepository.insert(order);

        // 保存明细并汇总
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderCreateRequest.ItemRequest itemReq : request.getItems()) {
                OOrderItem item = new OOrderItem();
                item.setOrderId(order.getId());
                item.setSkuName(itemReq.getSkuName());
                item.setSkuCode(itemReq.getSkuCode());
                item.setQuantity(itemReq.getQuantity());
                item.setWeightKg(itemReq.getWeightKg());
                item.setVolumeCbm(itemReq.getVolumeCbm());
                item.setUnitPrice(itemReq.getUnitPrice());
                orderItemRepository.insert(item);

                // 累加
                if (itemReq.getUnitPrice() != null && itemReq.getQuantity() != null) {
                    totalAmount = totalAmount.add(itemReq.getUnitPrice().multiply(new BigDecimal(itemReq.getQuantity())));
                }
                if (itemReq.getWeightKg() != null) {
                    totalWeight = totalWeight.add(itemReq.getWeightKg());
                }
                if (itemReq.getVolumeCbm() != null) {
                    totalVolume = totalVolume.add(itemReq.getVolumeCbm());
                }
            }
        }

        // 更新订单汇总
        if (request.getTotalAmount() == null) {
            order.setTotalAmount(totalAmount);
        }
        if (request.getWeightKg() == null) {
            order.setWeightKg(totalWeight);
        }
        if (request.getVolumeCbm() == null) {
            order.setVolumeCbm(totalVolume);
        }
        orderRepository.updateById(order);

        // 记录状态日志
        recordStatusLog(order.getId(), 10, null, "订单创建");

        log.info("创建订单: {}", orderNo);

        // 异步发送订单创建事件（用于下游仓储/运输模块）
        publishOrderCreatedEvent(order.getId(), orderNo);

        return toVO(order);
    }

    /**
     * 更新订单
     */
    @Transactional
    public OrderVO update(Long id, OrderCreateRequest request) {
        OOrder order = orderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 10 && order.getStatus() != 20) {
            throw new BusinessException("只有待确认或已确认状态的订单可以修改");
        }

        order.setSenderName(request.getSenderName());
        order.setSenderPhone(request.getSenderPhone());
        order.setSenderAddress(request.getSenderAddress());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        if (request.getTotalAmount() != null) order.setTotalAmount(request.getTotalAmount());
        if (request.getWeightKg() != null) order.setWeightKg(request.getWeightKg());
        if (request.getVolumeCbm() != null) order.setVolumeCbm(request.getVolumeCbm());
        if (request.getRemark() != null) order.setRemark(request.getRemark());

        orderRepository.updateById(order);
        log.info("更新订单: {}", order.getOrderNo());
        return toVO(order);
    }

    /**
     * 删除订单
     */
    @Transactional
    public void delete(Long id) {
        OOrder order = orderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() >= 30) {
            throw new BusinessException("已入库的订单无法删除");
        }
        // 删除明细
        LambdaQueryWrapper<OOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OOrderItem::getOrderId, id);
        orderItemRepository.delete(itemWrapper);
        // 删除日志
        LambdaQueryWrapper<OOrderStatusLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(OOrderStatusLog::getOrderId, id);
        statusLogRepository.delete(logWrapper);
        // 删除订单
        orderRepository.deleteById(id);
        log.info("删除订单: {}", order.getOrderNo());
    }

    /**
     * 更新订单状态
     */
    @Transactional
    public OrderVO updateStatus(Long id, Integer newStatus, String remark) {
        OOrder order = orderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        validateStatusTransition(order.getStatus(), newStatus);

        Integer oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.updateById(order);

        recordStatusLog(id, newStatus, null, remark != null ? remark : "状态变更");

        // 发布状态变更事件
        publishOrderStatusChangedEvent(id, order.getOrderNo(), oldStatus, newStatus);

        log.info("订单状态变更: {} {} -> {}",
                order.getOrderNo(), getStatusName(oldStatus), getStatusName(newStatus));
        return toVO(order);
    }

    /**
     * 查询订单状态日志
     */
    public List<StatusLogVO> getStatusLogs(Long orderId) {
        LambdaQueryWrapper<OOrderStatusLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OOrderStatusLog::getOrderId, orderId)
               .orderByAsc(OOrderStatusLog::getOperateTime);
        return statusLogRepository.selectList(wrapper).stream().map(this::toLogVO).toList();
    }

    // ==================== Private Methods ====================

    private void validateStatusTransition(Integer oldStatus, Integer newStatus) {
        // 允许的状态转换
        boolean valid = switch (oldStatus) {
            case 10 -> newStatus == 20 || newStatus == 80; // 待确认 -> 已确认/取消
            case 20 -> newStatus == 30 || newStatus == 80; // 已确认 -> 已入库/取消
            case 30 -> newStatus == 40 || newStatus == 80; // 已入库 -> 已发货/取消
            case 40 -> newStatus == 50; // 已发货 -> 运输中
            case 50 -> newStatus == 60 || newStatus == 70; // 运输中 -> 已送达/拒收
            case 60 -> newStatus == 70; // 已送达 -> 已完成
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("不允许的状态转换: " + getStatusName(oldStatus) + " -> " + getStatusName(newStatus));
        }
    }

    private void recordStatusLog(Long orderId, Integer status, Long operateBy, String remark) {
        OOrderStatusLog log = new OOrderStatusLog();
        log.setOrderId(orderId);
        log.setStatus(status);
        log.setOperateBy(operateBy);
        log.setOperateTime(LocalDateTime.now());
        log.setRemark(remark);
        statusLogRepository.insert(log);
    }

    @Async
    private void publishOrderCreatedEvent(Long orderId, String orderNo) {
        // RocketMQ 订单创建事件（Stage 3 仅作结构预留，暂不实际发送）
        log.info("[OrderEvent] 订单已创建: orderId={}, orderNo={}", orderId, orderNo);
    }

    @Async
    private void publishOrderStatusChangedEvent(Long orderId, String orderNo, Integer oldStatus, Integer newStatus) {
        log.info("[OrderEvent] 订单状态变更: orderId={}, orderNo={}, {} -> {}",
                orderId, orderNo, getStatusName(oldStatus), getStatusName(newStatus));
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMAT);
        long seq = (long) (Math.random() * 10000);
        return "ORD" + timestamp + String.format("%04d", seq);
    }

    private OrderVO toVO(OOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setSenderName(order.getSenderName());
        vo.setSenderPhone(order.getSenderPhone());
        vo.setSenderAddress(order.getSenderAddress());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setWeightKg(order.getWeightKg());
        vo.setVolumeCbm(order.getVolumeCbm());
        vo.setStatus(order.getStatus());
        vo.setStatusName(getStatusName(order.getStatus()));
        vo.setRemark(order.getRemark());
        vo.setCreatedBy(order.getCreatedBy());
        vo.setCreatedAt(order.getCreatedAt());

        // 查询创建人姓名
        if (order.getCreatedBy() != null) {
            userRepository.selectById(order.getCreatedBy());
        }

        // 查询明细
        LambdaQueryWrapper<OOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OOrderItem::getOrderId, order.getId());
        List<OOrderItem> items = orderItemRepository.selectList(itemWrapper);
        vo.setItems(items.stream().map(this::toItemVO).toList());

        // 查询状态日志
        LambdaQueryWrapper<OOrderStatusLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(OOrderStatusLog::getOrderId, order.getId())
                  .orderByAsc(OOrderStatusLog::getOperateTime);
        vo.setLogs(statusLogRepository.selectList(logWrapper).stream().map(this::toLogVO).toList());

        return vo;
    }

    private OrderItemVO toItemVO(OOrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setSkuName(item.getSkuName());
        vo.setSkuCode(item.getSkuCode());
        vo.setQuantity(item.getQuantity());
        vo.setWeightKg(item.getWeightKg());
        vo.setVolumeCbm(item.getVolumeCbm());
        vo.setUnitPrice(item.getUnitPrice());
        return vo;
    }

    private StatusLogVO toLogVO(OOrderStatusLog log) {
        StatusLogVO vo = new StatusLogVO();
        vo.setId(log.getId());
        vo.setStatus(log.getStatus());
        vo.setStatusName(getStatusName(log.getStatus()));
        vo.setOperateBy(log.getOperateBy());
        vo.setOperateTime(log.getOperateTime());
        vo.setRemark(log.getRemark());
        return vo;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 10 -> "待确认";
            case 20 -> "已确认";
            case 30 -> "已入库";
            case 40 -> "已发货";
            case 50 -> "运输中";
            case 60 -> "已送达";
            case 70 -> "已完成";
            case 80 -> "已取消";
            default -> "未知";
        };
    }
}
