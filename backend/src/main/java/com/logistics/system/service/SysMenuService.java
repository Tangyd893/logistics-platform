package com.logistics.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.common.exception.BusinessException;
import com.logistics.system.domain.dto.MenuCreateRequest;
import com.logistics.system.domain.entity.SysMenu;
import com.logistics.system.repository.SysMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysMenuService {

    @Autowired
    private SysMenuRepository menuRepository;

    public List<SysMenu> list() {
        return menuRepository.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getCreatedAt));
    }

    public SysMenu getById(Long id) {
        SysMenu menu = menuRepository.selectById(id);
        if (menu == null) throw new BusinessException("菜单不存在");
        return menu;
    }

    @Transactional
    public SysMenu create(MenuCreateRequest request) {
        SysMenu menu = new SysMenu();
        menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        menu.setName(request.getName());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        menu.setType(request.getType() != null ? request.getType() : 1);
        menu.setPerms(request.getPerms());
        menu.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        menuRepository.insert(menu);
        return menu;
    }

    @Transactional
    public SysMenu update(Long id, MenuCreateRequest request) {
        SysMenu menu = menuRepository.selectById(id);
        if (menu == null) throw new BusinessException("菜单不存在");
        menu.setName(request.getName());
        menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        if (request.getType() != null) menu.setType(request.getType());
        menu.setPerms(request.getPerms());
        if (request.getStatus() != null) menu.setStatus(request.getStatus());
        menuRepository.updateById(menu);
        return menu;
    }

    @Transactional
    public void delete(Long id) {
        long childCount = menuRepository.selectCount(
            new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) throw new BusinessException("请先删除子菜单");
        menuRepository.deleteById(id);
    }
}
