package com.huayang.product.framework.shiro.service;

import com.huayang.product.common.constant.Constants;
import com.huayang.product.common.constant.ShiroConstants;
import com.huayang.product.common.constant.UserConstants;
import com.huayang.product.common.core.domain.entity.SysRole;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.enums.UserStatus;
import com.huayang.product.common.exception.user.*;
import com.huayang.product.common.utils.*;
import com.huayang.product.framework.manager.AsyncManager;
import com.huayang.product.framework.manager.factory.AsyncFactory;
import com.huayang.product.system.service.SysConfigService;
import com.huayang.product.system.service.SysMenuService;
import com.huayang.product.system.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 登录校验方法
 *
 * @author huayang
 */
@Component
public class SysLoginService {
    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysMenuService menuService;

    @Autowired
    private SysConfigService configService;

    /**
     * 登录
     */
    public SysUser login(String username, String password) {
        // 验证码校验
        if (ShiroConstants.CAPTCHA_ERROR.equals(ServletUtils.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA))) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }

        // IP黑名单校验
        String blackStr = configService.selectByConfigKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, ShiroUtils.getIp())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }

        // 查询用户信息
        SysUser user = userService.selectUserByLoginName(username);
        /**
         if (user == null && maybeMobilePhoneNumber(username))
         {
         user = userService.selectUserByPhoneNumber(username);
         }

         if (user == null && maybeEmail(username))
         {
         user = userService.selectUserByEmail(username);
         }
         */

        if (user == null) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.not.exists")));
            throw new UserNotExistsException();
        }

        if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.delete")));
            throw new UserDeleteException();
        }

        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.blocked")));
            throw new UserBlockedException();
        }

        passwordService.validate(user, password);

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        setRolePermission(user);
        recordLoginInfo(user.getUserId());
        return user;
    }

    /**
     * 设置角色权限
     *
     * @param user 用户信息
     */
    public void setRolePermission(SysUser user) {
        List<SysRole> roles = user.getRoles();
        if (!roles.isEmpty()) {
            // 设置permissions属性，以便数据权限匹配权限
            for (SysRole role : roles) {
                Set<String> rolePerms = menuService.selectPermsByRoleId(role.getRoleId());
                role.setPermissions(rolePerms);
            }
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setLoginIp(ShiroUtils.getIp());
        user.setLoginDate(DateUtils.getNowDate());
        userService.updateUserInfo(user);
    }
}
