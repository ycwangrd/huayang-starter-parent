package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.annotation.DataScope;
import com.huayang.product.common.constant.UserConstants;
import com.huayang.product.common.core.domain.Ztree;
import com.huayang.product.common.core.domain.entity.SysDept;
import com.huayang.product.common.core.domain.entity.SysRole;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.exception.ServiceException;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.common.utils.ShiroUtils;
import com.huayang.product.framework.web.service.CacheService;
import com.huayang.product.system.mapper.SysDeptMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理 服务实现
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> implements BaseService<SysDept> {
    private static final String CACHE_NAME = "sysDept";

    @Autowired
    private CacheService cacheService;


    /**
     * 查询部门管理数据
     *
     * @param dept 部门信息
     * @return 部门信息集合
     */
    @DataScope(deptAlias = "d")
    public List<SysDept> selectList(SysDept dept) {
        return baseMapper.selectDeptList(dept);
    }

    /**
     * 查询部门管理树
     *
     * @param dept 部门信息
     * @return 所有部门信息
     */
    @DataScope(deptAlias = "d")
    public List<Ztree> selectDeptTree(SysDept dept) {
        List<SysDept> deptList = baseMapper.selectDeptList(dept);
        List<Ztree> ztrees = initZtree(deptList);
        return ztrees;
    }

    /**
     * 查询部门管理树（排除下级）
     *
     * @param dept 部门
     * @return 所有部门信息
     */
    @DataScope(deptAlias = "d")
    public List<Ztree> selectDeptTreeExcludeChild(SysDept dept) {
        Long excludeId = dept.getExcludeId();
        List<SysDept> depts = baseMapper.selectDeptList(dept);
        if (excludeId.intValue() > 0) {
            depts.removeIf(d -> d.getDeptId().intValue() == excludeId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), excludeId + ""));
        }
        List<Ztree> ztrees = initZtree(depts);
        return ztrees;
    }

    /**
     * 根据角色ID查询部门（数据权限）
     *
     * @param role 角色对象
     * @return 部门列表（数据权限）
     */
    public List<Ztree> roleDeptTreeData(SysRole role) {
        Long roleId = role.getRoleId();
        List<Ztree> ztrees;
        List<SysDept> deptList = SpringUtil.getBean(this.getClass()).selectList(new SysDept());
        if (ObjUtil.isNotNull(roleId)) {
            List<String> roleDeptList = baseMapper.selectRoleDeptTree(roleId);
            ztrees = initZtree(deptList, roleDeptList);
        } else {
            ztrees = initZtree(deptList);
        }
        return ztrees;
    }

    /**
     * 对象转部门树
     *
     * @param deptList 部门列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysDept> deptList) {
        return initZtree(deptList, null);
    }

    /**
     * 对象转部门树
     *
     * @param deptList     部门列表
     * @param roleDeptList 角色已存在菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysDept> deptList, List<String> roleDeptList) {
        List<Ztree> ztrees = new ArrayList<>();
        boolean isCheck = CollUtil.isNotEmpty(roleDeptList);
        for (SysDept dept : deptList) {
            if (UserConstants.DEPT_NORMAL.equals(dept.getStatus())) {
                Ztree ztree = new Ztree();
                ztree.setId(dept.getDeptId());
                ztree.setpId(dept.getParentId());
                ztree.setName(dept.getDeptName());
                ztree.setTitle(dept.getDeptName());
                if (isCheck) {
                    ztree.setChecked(roleDeptList.contains(dept.getDeptId() + dept.getDeptName()));
                }
                ztrees.add(ztree);
            }
        }
        return ztrees;
    }

    /**
     * 根据父部门ID查询下级部门数量
     *
     * @param parentId 部门ID
     * @return 结果
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == 0")
    public long selectDeptCount(Long parentId) {
        LambdaQueryWrapper<SysDept> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysDept::getParentId, parentId);
        queryWrapper.eq(SysDept::getDelFlag, UserConstants.DEPT_NORMAL);
        return count(queryWrapper);
    }

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long deptId) {
        return removeById(deptId);
    }

    /**
     * 新增保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDept(SysDept dept) {
        SysDept info = getById(dept.getParentId());
        // 如果父节点不为"正常"状态,则不允许新增子节点
        if (!UserConstants.DEPT_NORMAL.equals(info.getStatus())) {
            throw new ServiceException("部门停用，不允许新增");
        }
        dept.setCreateTime(DateUtils.getNowDate());
        dept.setAncestors(info.getAncestors() + "," + dept.getParentId());
        dept.setDelFlag(UserConstants.DEPT_NORMAL);
        return save(dept);
    }

    /**
     * 修改保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(SysDept dept) {
        SysDept newParentDept = getById(dept.getParentId());
        SysDept oldDept = getById(dept.getDeptId());
        if (ObjUtil.isNotNull(newParentDept) && ObjUtil.isNotNull(oldDept)) {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
        }
        boolean result = updateById(dept);
        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StringUtils.isNotEmpty(dept.getAncestors())
                && !StringUtils.equals("0", dept.getAncestors())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     *
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDept dept) {
        String ancestors = dept.getAncestors();
        Long[] deptIds = ConvertUtil.toLongArray(ancestors);
        LambdaUpdateWrapper<SysDept> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(SysDept::getStatus, UserConstants.DEPT_NORMAL);
        updateWrapper.in(SysDept::getDeptId, deptIds);
        update(updateWrapper);
    }

    /**
     * 修改子元素关系
     *
     * @param deptId       被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDept> children = list(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, deptId));
        for (SysDept child : children) {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0) {
            baseMapper.updateDeptChildren(children);
        }
    }

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == null")
    public SysDept selectById(Long deptId) {
        return getById(deptId);
    }

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == 0")
    public int selectNormalChildrenDeptById(Long deptId) {
        return baseMapper.selectNormalChildrenDeptById(deptId);
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == false")
    public boolean checkDeptNameUnique(SysDept dept) {
        Long deptId = ObjUtil.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        LambdaQueryWrapper<SysDept> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysDept::getDeptName, dept.getDeptName());
        queryWrapper.eq(SysDept::getParentId, dept.getParentId());
        queryWrapper.eq(SysDept::getDelFlag, UserConstants.DEPT_NORMAL);
        queryWrapper.ne(SysDept::getDeptId, deptId);
        List<SysDept> deptList = list(new Page<>(1, 1), queryWrapper);
        return CollUtil.isNotEmpty(deptList) ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验部门是否有数据权限
     *
     * @param deptId 部门id
     */
    public void checkDeptDataScope(Long deptId) {
        if (!SysUser.isAdmin(ShiroUtils.getUserId()) && ObjUtil.isNotNull(deptId)) {
            SysDept dept = new SysDept();
            dept.setDeptId(deptId);
            List<SysDept> depts = SpringUtil.getBean(this.getClass()).selectList(dept);
            if (CollUtil.isEmpty(depts)) {
                throw new ServiceException("没有权限访问部门数据！");
            }
        }
    }
}
