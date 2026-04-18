package com.logistics.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.dto.PageDTO;
import com.logistics.common.exception.BusinessException;
import com.logistics.system.domain.dto.UserCreateRequest;
import com.logistics.system.domain.entity.SysUser;
import com.logistics.system.domain.vo.UserVO;
import com.logistics.system.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserService.class);

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SysUserService(SysUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 分页查询用户
     */
    public PageDTO<UserVO> page(String keyword, Long deptId, Integer status, int page, int size) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getDisplayName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> result = userRepository.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return PageDTO.of(voList, result.getTotal(), page, size);
    }

    /**
     * 根据ID查询用户
     */
    public UserVO getById(Long id) {
        SysUser user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    /**
     * 创建用户
     */
    @Transactional
    public UserVO create(UserCreateRequest request) {
        if (userRepository.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        user.setWarehouseId(request.getWarehouseId());
        user.setRoleCode(request.getRoleCode());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setPassword(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "123456"));

        userRepository.insert(user);
        log.info("创建用户: {}", request.getUsername());
        return toVO(user);
    }

    /**
     * 更新用户
     */
    @Transactional
    public UserVO update(Long id, UserCreateRequest request) {
        SysUser user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setDisplayName(request.getDisplayName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        user.setWarehouseId(request.getWarehouseId());
        user.setRoleCode(request.getRoleCode());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.updateById(user);
        log.info("更新用户: {}", user.getUsername());
        return toVO(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void delete(Long id) {
        SysUser user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userRepository.deleteById(id);
        log.info("删除用户: {}", user.getUsername());
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUser user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.updateById(user);
        log.info("重置用户密码: {}", user.getUsername());
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setDeptId(user.getDeptId());
        vo.setWarehouseId(user.getWarehouseId());
        vo.setRoleCode(user.getRoleCode());
        vo.setStatus(user.getStatus());
        vo.setStatusName(user.getStatus() == 1 ? "启用" : "禁用");
        return vo;
    }
}
