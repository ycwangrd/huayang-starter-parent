package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.huayang.product.common.utils.bean.BeanValidators;
import com.huayang.product.common.utils.security.Md5Utils;
import com.huayang.product.system.domain.SysPost;
import com.huayang.product.system.domain.SysUserPost;
import com.huayang.product.system.domain.SysUserRole;
import com.huayang.product.system.mapper.SysPostMapper;
import com.huayang.product.system.mapper.SysRoleMapper;
import com.huayang.product.system.mapper.SysUserMapper;
import com.huayang.product.system.mapper.SysUserRoleMapper;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {
    @Autowired
    protected Validator validator;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysPostMapper postMapper;
    @Autowired
    private SysPostService postService;
    @Autowired
    private SysUserPostService userPostService;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysUserRoleService userRoleService;
    @Autowired
    private SysConfigService configService;
    @Autowired
    private SysDeptService deptService;

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectList(SysUser user) {
        return userMapper.selectUserList(user);
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user) {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user) {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    public SysUser selectUserByLoginName(String userName) {
        return userMapper.selectUserByLoginName(userName);
    }

    /**
     * 通过手机号码查询用户
     *
     * @param phoneNumber 手机号码
     * @return 用户对象信息
     */
    public SysUser selectUserByPhoneNumber(String phoneNumber) {
        return userMapper.selectUserByPhoneNumber(phoneNumber);
    }

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    public SysUser selectUserByEmail(String email) {
        return userMapper.selectUserByEmail(email);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public SysUser selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 通过用户ID查询用户和角色关联
     *
     * @param userId 用户ID
     * @return 用户和角色关联列表
     */
    public List<SysUserRole> selectUserRoleByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        return userRoleService.list(queryWrapper);
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserById(Long userId) {
        // 删除用户与角色关联
        LambdaQueryWrapper<SysUserRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        userRoleService.remove(queryWrapper);
        // 删除用户与岗位表
        LambdaQueryWrapper<SysUserPost> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserPost::getUserId, userId);
        userPostService.remove(wrapper);
        return removeById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(String ids) {
        Long[] userIds = ConvertUtil.toLongArray(ids);
        for (Long userId : userIds) {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        LambdaQueryWrapper<SysUserRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(SysUserRole::getUserId, userIds);
        userRoleService.remove(queryWrapper);
        // 删除用户与岗位关联
        LambdaQueryWrapper<SysUserPost> wrapper = Wrappers.lambdaQuery();
        wrapper.in(SysUserPost::getUserId, userIds);
        userPostService.remove(wrapper);
        return removeByIds(Arrays.asList(userIds));
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(SysUser user) {
        user.setCreateTime(DateUtils.getNowDate());
        // 新增用户信息
        boolean result = save(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user.getUserId(), user.getRoleIds());
        return result;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(SysUser user) {
        user.setUserType(UserConstants.REGISTER_USER_TYPE);
        user.setCreateTime(DateUtils.getNowDate());
        return save(user);
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 新增用户与角色管理
        insertUserRole(user.getUserId(), user.getRoleIds());
        // 删除用户与岗位关联
        userPostService.remove(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        // 新增用户与岗位管理
        insertUserPost(user);
        user.setUpdateTime(DateUtils.getNowDate());
        return updateById(user);
    }

    /**
     * 修改用户个人详细信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(SysUser user) {
        return updateById(user);
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserPwd(SysUser user) {
        user.setUpdateTime(DateUtils.getNowDate());
        return updateById(user);
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUserRole(Long userId, Long[] roleIds) {
        if (ArrayUtil.isNotEmpty(roleIds)) {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (CollUtil.isNotEmpty(list)) {
                userRoleService.saveBatch(list);
            }
        }
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUserPost(SysUser user) {
        Long[] posts = user.getPostIds();
        if (ArrayUtil.isNotEmpty(posts)) {
            // 新增用户与岗位管理
            List<SysUserPost> list = new ArrayList<>();
            for (Long postId : posts) {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            if (CollUtil.isNotEmpty(list)) {
                userPostService.saveBatch(list);
            }
        }
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean checkLoginNameUnique(SysUser user) {
        Long userId = ObjUtil.isNull(user.getUserId()) ? -1L : user.getUserId();
        LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUser::getLoginName, user.getLoginName());
        queryWrapper.eq(SysUser::getDelFlag, "0");
        queryWrapper.ne(SysUser::getUserId, userId);
        return CollUtil.isNotEmpty(list(new Page<>(1, 1), queryWrapper)) ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    public boolean checkPhoneUnique(SysUser user) {
        Long userId = ObjUtil.isNull(user.getUserId()) ? -1L : user.getUserId();
        LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUser::getPhonenumber, user.getPhonenumber());
        queryWrapper.eq(SysUser::getDelFlag, "0");
        queryWrapper.ne(SysUser::getUserId, userId);
        return CollUtil.isNotEmpty(list(new Page<>(1, 1), queryWrapper)) ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    public boolean checkEmailUnique(SysUser user) {
        Long userId = ObjUtil.isNull(user.getUserId()) ? -1L : user.getUserId();
        LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUser::getEmail, user.getEmail());
        queryWrapper.eq(SysUser::getDelFlag, "0");
        queryWrapper.ne(SysUser::getUserId, userId);
        return CollUtil.isNotEmpty(list(new Page<>(1, 1), queryWrapper)) ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    public void checkUserAllowed(SysUser user) {
        if (ObjUtil.isNotNull(user.getUserId()) && user.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    public void checkUserDataScope(Long userId) {
        if (!SysUser.isAdmin(ShiroUtils.getUserId())) {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtil.getBean(this.getClass()).selectList(user);
            if (CollUtil.isEmpty(users)) {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    public String selectUserRoleGroup(Long userId) {
        List<SysRole> list = roleMapper.selectRolesByUserId(userId);
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    public String selectUserPostGroup(Long userId) {
        List<SysPost> list = postMapper.selectPostsByUserId(userId);
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 导入用户数据
     *
     * @param userList        用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName) {
        if (CollUtil.isEmpty(userList)) {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUser user : userList) {
            try {
                // 验证是否存在这个用户
                LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(SysUser::getLoginName, user.getLoginName());
                queryWrapper.eq(SysUser::getDelFlag, "0");
                List<SysUser> users = list(new Page<>(1, 1), queryWrapper);
                if (CollUtil.isEmpty(users)) {
                    BeanValidators.validateWithException(validator, user);
                    deptService.checkDeptDataScope(user.getDeptId());
                    String password = configService.selectByConfigKey("sys.user.initPassword");
                    user.setPassword(Md5Utils.hash(user.getLoginName() + password));
                    user.setCreateBy(operName);
                    save(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 导入成功");
                } else if (isUpdateSupport) {
                    SysUser sysUser = users.get(0);
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(sysUser);
                    checkUserDataScope(sysUser.getUserId());
                    deptService.checkDeptDataScope(user.getDeptId());
                    user.setUserId(sysUser.getUserId());
                    user.setUpdateBy(operName);
                    updateById(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getLoginName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getLoginName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 用户状态修改
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(SysUser user) {
        user.setUpdateTime(DateUtils.getNowDate());
        return updateById(user);
    }

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean checkDeptExistUser(Long deptId) {
        LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUser::getDeptId, deptId);
        queryWrapper.eq(SysUser::getDelFlag, UserConstants.NORMAL);
        return count(queryWrapper) > 0;
    }
}
