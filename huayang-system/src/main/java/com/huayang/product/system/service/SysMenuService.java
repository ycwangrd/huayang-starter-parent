package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.constant.UserConstants;
import com.huayang.product.common.core.domain.Ztree;
import com.huayang.product.common.core.domain.entity.SysMenu;
import com.huayang.product.common.core.domain.entity.SysRole;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.system.domain.SysRoleMenu;
import com.huayang.product.system.mapper.SysMenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
 * 菜单 业务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {
    public static final String PREMISSION_STRING = "perms[\"{0}\"]";
    @Autowired
    private SysRoleMenuService roleMenuService;

    /**
     * 根据用户查询菜单
     *
     * @param user 用户信息
     * @return 菜单列表
     */
    public List<SysMenu> selectMenusByUser(SysUser user) {
        List<SysMenu> menus;
        // 管理员显示所有菜单信息
        if (user.isAdmin()) {
            menus = baseMapper.selectMenuNormalAll();
        } else {
            menus = baseMapper.selectMenusByUserId(user.getUserId());
        }
        return getChildPerms(menus, 0);
    }

    /**
     * 查询菜单集合
     *
     * @return 所有菜单信息
     */
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId) {
        List<SysMenu> menuList;
        if (SysUser.isAdmin(userId)) {
            LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
            if (StringUtils.isNotBlank(menu.getMenuName())) {
                queryWrapper.like(SysMenu::getMenuName, menu.getMenuName());
            }
            if (StringUtils.isNotBlank(menu.getVisible())) {
                queryWrapper.eq(SysMenu::getVisible, Boolean.valueOf(menu.getVisible()));
            }
            // 排序
            queryWrapper.orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getOrderNum);
            menuList = list(queryWrapper);
        } else {
            menu.getParams().put("userId", userId);
            menuList = baseMapper.selectMenuListByUserId(menu);
        }
        return menuList;
    }

    /**
     * 查询菜单集合
     *
     * @return 所有菜单信息
     */
    public List<SysMenu> selectMenuAll(Long userId) {
        List<SysMenu> menuList;
        if (SysUser.isAdmin(userId)) {
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getOrderNum);
            menuList = list(queryWrapper);
        } else {
            menuList = baseMapper.selectMenuAllByUserId(userId);
        }
        return menuList;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectPermsByUserId(Long userId) {
        List<String> perms = baseMapper.selectPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    public Set<String> selectPermsByRoleId(Long roleId) {
        List<String> perms = baseMapper.selectPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询菜单
     *
     * @param role 角色对象
     * @return 菜单列表
     */
    public List<Ztree> roleMenuTreeData(SysRole role, Long userId) {
        Long roleId = role.getRoleId();
        List<Ztree> ztrees;
        List<SysMenu> menuList = selectMenuAll(userId);
        if (ObjUtil.isNotNull(roleId)) {
            List<String> roleMenuList = baseMapper.selectMenuTree(roleId);
            ztrees = initZtree(menuList, roleMenuList, true);
        } else {
            ztrees = initZtree(menuList, null, true);
        }
        return ztrees;
    }

    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     */
    public List<Ztree> menuTreeData(Long userId) {
        List<SysMenu> menuList = selectMenuAll(userId);
        List<Ztree> ztrees = initZtree(menuList);
        return ztrees;
    }

    /**
     * 查询系统所有权限
     *
     * @return 权限列表
     */
    public LinkedHashMap<String, String> selectPermsAll(Long userId) {
        LinkedHashMap<String, String> section = new LinkedHashMap<>();
        List<SysMenu> permissions = selectMenuAll(userId);
        if (CollUtil.isNotEmpty(permissions)) {
            for (SysMenu menu : permissions) {
                section.put(menu.getUrl(), MessageFormat.format(PREMISSION_STRING, menu.getPerms()));
            }
        }
        return section;
    }

    /**
     * 对象转菜单树
     *
     * @param menuList 菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> menuList) {
        return initZtree(menuList, null, false);
    }

    /**
     * 对象转菜单树
     *
     * @param menuList     菜单列表
     * @param roleMenuList 角色已存在菜单列表
     * @param permsFlag    是否需要显示权限标识
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> menuList, List<String> roleMenuList, boolean permsFlag) {
        List<Ztree> ztrees = new ArrayList<>();
        boolean isCheck = CollUtil.isNotEmpty(roleMenuList);
        for (SysMenu menu : menuList) {
            Ztree ztree = new Ztree();
            ztree.setId(menu.getMenuId());
            ztree.setpId(menu.getParentId());
            ztree.setName(transMenuName(menu, permsFlag));
            ztree.setTitle(menu.getMenuName());
            if (isCheck) {
                ztree.setChecked(roleMenuList.contains(menu.getMenuId() + menu.getPerms()));
            }
            ztrees.add(ztree);
        }
        return ztrees;
    }

    public String transMenuName(SysMenu menu, boolean permsFlag) {
        StringBuffer sb = new StringBuffer();
        sb.append(menu.getMenuName());
        if (permsFlag) {
            sb.append("<font color=\"#888\">&nbsp;&nbsp;&nbsp;" + menu.getPerms() + "</font>");
        }
        return sb.toString();
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long menuId) {
        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.or().eq(SysMenu::getParentId, menuId);
        queryWrapper.or().eq(SysMenu::getMenuId, menuId);
        return remove(queryWrapper);
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    public SysMenu selectMenuById(Long menuId) {
        return baseMapper.selectMenuById(menuId);
    }

    /**
     * 查询子菜单数量
     *
     * @param parentId 父级菜单ID
     * @return 结果
     */
    public long selectCountMenuByParentId(Long parentId) {
        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysMenu::getParentId, parentId);
        return count(queryWrapper);
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public long selectCountRoleMenuByMenuId(Long menuId) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, menuId);
        return roleMenuService.count(queryWrapper);
    }

    /**
     * 新增保存菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertMenu(SysMenu menu) {
        menu.setCreateTime(DateUtils.getNowDate());
        return save(menu);
    }

    /**
     * 修改保存菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(SysMenu menu) {
        menu.setUpdateTime(DateUtils.getNowDate());
        return updateById(menu);
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean checkMenuNameUnique(SysMenu menu) {
        Long menuId = ObjUtil.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysMenu::getMenuName, menu.getMenuName());
        queryWrapper.eq(SysMenu::getParentId, menu.getParentId());
        queryWrapper.ne(SysMenu::getMenuId, menuId);
        return count(queryWrapper) > 0 ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tlist = new ArrayList<>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0;
    }
}
