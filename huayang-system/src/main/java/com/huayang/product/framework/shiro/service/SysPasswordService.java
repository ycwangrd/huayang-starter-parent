package com.huayang.product.framework.shiro.service;

import com.huayang.product.common.constant.Constants;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.core.redis.RedisCache;
import com.huayang.product.common.exception.user.UserPasswordNotMatchException;
import com.huayang.product.common.exception.user.UserPasswordRetryLimitExceedException;
import com.huayang.product.common.utils.MessageUtils;
import com.huayang.product.common.utils.security.Md5Utils;
import com.huayang.product.framework.manager.AsyncManager;
import com.huayang.product.framework.manager.factory.AsyncFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录密码方法
 *
 * @author ruoyi
 */
@Component
public class SysPasswordService {
    /**
     * 登录记录 cache key
     */
    private final String SYS_LOGINRECORDCACHE_KEY = "sys_loginRecordCache:";
    @Autowired
    private RedisCache redisCache;
    @Value(value = "${user.password.maxRetryCount}")
    private String maxRetryCount;

    /**
     * 设置cache key
     *
     * @param loginName 登录名
     * @return 缓存键key
     */
    private String getCacheKey(String loginName) {
        return SYS_LOGINRECORDCACHE_KEY + loginName;
    }

    public void validate(SysUser user, String password) {
        String loginName = user.getLoginName();

        Integer retryCount = redisCache.getCacheObject(getCacheKey(loginName));

        if (retryCount == null) {
            retryCount = 0;
            redisCache.setCacheObject(getCacheKey(loginName), retryCount, 10, TimeUnit.MINUTES);
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue()) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.LOGIN_FAIL, MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount)));
            throw new UserPasswordRetryLimitExceedException(Integer.valueOf(maxRetryCount).intValue());
        }

        if (!matches(user, password)) {
            retryCount = retryCount + 1;
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.LOGIN_FAIL, MessageUtils.message("user.password.retry.limit.count", retryCount)));
            redisCache.setCacheObject(getCacheKey(loginName), retryCount, 10, TimeUnit.MINUTES);
            throw new UserPasswordNotMatchException();
        } else {
            clearLoginRecordCache(loginName);
        }
    }

    public boolean matches(SysUser user, String newPassword) {
        return user.getPassword().equals(encryptPassword(user.getLoginName(), newPassword, user.getSalt()));
    }

    public void clearLoginRecordCache(String loginName) {
        redisCache.deleteObject(getCacheKey(loginName));
    }

    public String encryptPassword(String loginName, String password, String salt) {
        return Md5Utils.hash(loginName + password + salt);
    }
}
