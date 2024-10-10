package com.huayang.product.framework.shiro.web.filter;

import com.huayang.product.common.constant.Constants;
import com.huayang.product.common.constant.ShiroConstants;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.utils.MessageUtils;
import com.huayang.product.common.utils.ShiroUtils;
import com.huayang.product.framework.manager.AsyncManager;
import com.huayang.product.framework.manager.factory.AsyncFactory;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.util.ObjUtil;

import java.io.Serializable;
import java.util.Deque;

/**
 * 退出过滤器
 *
 * @author ruoyi
 */
@Slf4j
public class LogoutFilter extends org.apache.shiro.web.filter.authc.LogoutFilter {
    /**
     * 退出后重定向的地址
     */
    @Setter
    @Getter
    private String loginUrl;

    private Cache<String, Deque<Serializable>> cache;

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) {
        try {
            Subject subject = getSubject(request, response);
            String redirectUrl = getRedirectUrl(request, response, subject);
            try {
                SysUser user = ShiroUtils.getSysUser();
                if (ObjUtil.isNotNull(user)) {
                    String loginName = user.getLoginName();
                    // 记录用户退出日志
                    AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
                    // 清理缓存
                    removeUserCache(loginName, ShiroUtils.getSessionId());
                }
                // 退出登录
                subject.logout();
            } catch (SessionException ise) {
                log.error("logout fail.", ise);
            }
            issueRedirect(request, response, redirectUrl);
        } catch (Exception e) {
            log.error("Encountered session exception during logout.  This can generally safely be ignored.", e);
        }
        return false;
    }

    public void removeUserCache(String loginName, String sessionId) {
        Deque<Serializable> deque = cache.get(loginName);
        if (CollUtil.isEmpty(deque)) {
            return;
        }
        deque.remove(sessionId);
        cache.put(loginName, deque);
    }

    /**
     * 退出跳转URL
     */
    @Override
    protected String getRedirectUrl(ServletRequest request, ServletResponse response, Subject subject) {
        String url = getLoginUrl();
        if (StringUtils.isNotEmpty(url)) {
            return url;
        }
        return super.getRedirectUrl(request, response, subject);
    }

    // 设置Cache的key的前缀
    public void setCacheManager(CacheManager cacheManager) {
        // 必须和ehcache缓存配置中的缓存name一致
        this.cache = cacheManager.getCache(ShiroConstants.SYS_USERCACHE);
    }
}
