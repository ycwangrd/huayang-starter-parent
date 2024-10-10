package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.annotation.DataScope;
import com.huayang.product.common.constant.UserConstants;
import com.huayang.product.common.core.domain.entity.SysRole;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.exception.ServiceException;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.common.utils.ShiroUtils;
import com.huayang.product.system.domain.SysRoleDept;
import com.huayang.product.system.domain.SysRoleMenu;
import com.huayang.product.system.domain.SysUserRole;
import com.huayang.product.system.mapper.SysRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色 业务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {
    @Autowired
    private SysRoleMenuService roleMenuService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysRoleDeptService roleDeptService;

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @DataScope(deptAlias = "d")
    public List<SysRole> selectList(SysRole role) {
        return baseMapper.selectRoleList(role);
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectRoleKeys(Long userId) {
        List<SysRole> perms = baseMapper.selectRolesByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (ObjUtil.isNotNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<SysRole> selectRolesByUserId(Long userId) {
        List<SysRole> userRoles = baseMapper.selectRolesByUserId(userId);
        Set<Long> roleSet = userRoles.stream().map(SysRole::getRoleId).collect(Collectors.toSet());
        List<SysRole> roles = list(new LambdaQueryWrapper<SysRole>().eq(SysRole::getDelFlag, UserConstants.NORMAL));
        for (SysRole role : roles) {
            if (roleSet.contains(role.getRoleId())) {
                role.setFlag(true);
            }
        }
        return roles;
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long roleId) {
        // 删除角色与菜单关联
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRoleMenu::getRoleId, roleId);
        roleMenuService.remove(queryWrapper);
        // 删除角色与部门关联
        LambdaQueryWrapper<SysRoleDept> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRoleDept::getRoleId, roleId);
        roleDeptService.remove(wrapper);
        // 更新状态为删除
        LambdaUpdateWrapper<SysRole> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(SysRole::getRoleId, roleId);
        updateWrapper.set(SysRole::getDelFlag, '2');
        return update(updateWrapper);
    }

    /**
     * 批量删除角色信息
     *
     * @param ids 需要删除的数据ID
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(String ids) {
        Long[] roleIds = ConvertUtil.toLongArray(ids);
        for (Long roleId : roleIds) {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = getById(roleId);
            // 查询已经分配的角色
            LambdaUpdateWrapper<SysUserRole> queryWrapper = Wrappers.lambdaUpdate();
            queryWrapper.eq(SysUserRole::getRoleId, roleId);
            if (sysUserRoleService.count(queryWrapper) > 0) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        LambdaUpdateWrapper<SysRoleMenu> queryWrapper = Wrappers.lambdaUpdate();
        queryWrapper.in(SysRoleMenu::getRoleId, roleIds);
        roleMenuService.remove(queryWrapper);
        // 删除角色与部门关联
        LambdaUpdateWrapper<SysRoleDept> wrapper = Wrappers.lambdaUpdate();
        wrapper.in(SysRoleDept::getRoleId, roleIds);
        roleDeptService.remove(wrapper);
        // 更新状态为删除
        LambdaUpdateWrapper<SysRole> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(SysRole::getRoleId, roleIds);
        updateWrapper.set(SysRole::getDelFlag, '2');
        return update(updateWrapper);
    }

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(SysRole role) {
        role.setCreateTime(DateUtils.getNowDate());
        // 新增角色信息
        save(role);
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        role.setUpdateTime(DateUtils.getNowDate());
        // 修改角色信息
        updateById(role);
        // 删除角色与菜单关联
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRoleMenu::getRoleId, role.getRoleId());
        roleMenuService.remove(queryWrapper);
        return insertRoleMenu(role);
    }

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean authDataScope(SysRole role) {
        role.setUpdateTime(DateUtils.getNowDate());
        // 修改角色信息
        updateById(role);
        // 删除角色与部门关联
        LambdaQueryWrapper<SysRoleDept> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRoleDept::getRoleId, role.getRoleId());
        roleDeptService.remove(queryWrapper);
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRoleMenu(SysRole role) {
        boolean result = true;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<>();
        for (Long menuId : role.getMenuIds()) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (CollUtil.isNotEmpty(list)) {
            result = roleMenuService.saveBatch(list);
        }
        return result;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRoleDept(SysRole role) {
        boolean result = true;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<>();
        for (Long deptId : role.getDeptIds()) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (CollUtil.isNotEmpty(list)) {
            result = roleDeptService.saveBatch(list);
        }
        return result;
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    public boolean checkRoleNameUnique(SysRole role) {
        Long roleId = ObjUtil.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRole::getRoleName, role.getRoleName());
        queryWrapper.ne(SysRole::getRoleId, roleId);
        List<SysRole> roleList = list(new Page<>(1, 1), queryWrapper);
        return CollUtil.isNotEmpty(roleList) ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    public boolean checkRoleKeyUnique(SysRole role) {
        Long roleId = ObjUtil.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRole::getRoleKey, role.getRoleKey());
        queryWrapper.ne(SysRole::getRoleId, roleId);
        List<SysRole> roleList = list(new Page<>(1, 1), queryWrapper);
        return CollUtil.isNotEmpty(roleList) ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    public void checkRoleAllowed(SysRole role) {
        if (ObjUtil.isNotNull(role.getRoleId()) && role.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleIds 角色id
     */
    public void checkRoleDataScope(Long... roleIds) {
        if (!SysUser.isAdmin(ShiroUtils.getUserId())) {
            for (Long roleId : roleIds) {
                SysRole role = new SysRole();
                role.setRoleId(roleId);
                List<SysRole> roles = SpringUtil.getBean(this.getClass()).selectList(role);
                if (CollUtil.isEmpty(roles)) {
                    throw new ServiceException("没有权限访问角色数据！");
                }
            }
        }
    }

    /**
     * 角色状态修改
     *
     * @param role 角色信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(SysRole role) {
        return updateRole(role);
    }

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAuthUser(SysUserRole userRole) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUserRole::getUserId, userRole.getUserId());
        queryWrapper.eq(SysUserRole::getRoleId, userRole.getRoleId());
        return sysUserRoleService.remove(queryWrapper);
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAuthUsers(Long roleId, String userIds) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUserRole::getRoleId, roleId);
        queryWrapper.in(SysUserRole::getUserId, Arrays.asList(ConvertUtil.toLongArray(userIds)));
        return sysUserRoleService.remove(queryWrapper);
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertAuthUsers(Long roleId, String userIds) {
        Long[] users = ConvertUtil.toLongArray(userIds);
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<>();
        for (Long userId : users) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return sysUserRoleService.saveBatch(list);
    }

    /**
     * 查询角色信息
     *
     * @param roleId
     * @return
     */
    public SysRole selectById(Long roleId) {
        return getById(roleId);
    }
}
