package com.huayang.product.framework.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.huayang.product.common.constant.Constants;
import com.huayang.product.common.utils.security.CipherUtils;
import com.huayang.product.framework.shiro.realm.UserRealm;
import com.huayang.product.framework.shiro.web.CustomShiroFilterFactoryBean;
import com.huayang.product.framework.shiro.web.filter.LogoutFilter;
import com.huayang.product.framework.shiro.web.filter.captcha.CaptchaValidateFilter;
import com.huayang.product.framework.shiro.web.filter.kickout.KickoutSessionFilter;
import jakarta.servlet.Filter;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.lang.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限配置加载
 *
 * @author ruoyi
 */
@Configuration
public class ShiroConfig {
    /**
     * Session超时时间，单位为毫秒（默认30分钟）
     */
    @Value("${shiro.session.expireTime}")
    private int expireTime;

    /**
     * 相隔多久检查一次session的有效性，单位毫秒，默认就是10分钟
     */
    @Value("${shiro.session.validationInterval}")
    private int validationInterval;

    /**
     * 同一个用户最大会话数
     */
    @Value("${shiro.session.maxSession}")
    private int maxSession;

    /**
     * 踢出之前登录的/之后登录的用户，默认踢出之前登录的用户
     */
    @Value("${shiro.session.kickoutAfter}")
    private boolean kickoutAfter;

    /**
     * 验证码开关
     */
    @Value("${shiro.user.captchaEnabled}")
    private boolean captchaEnabled;

    /**
     * 验证码类型
     */
    @Value("${shiro.user.captchaType}")
    private String captchaType;

    /**
     * 设置Cookie的域名
     */
    @Value("${shiro.cookie.domain}")
    private String domain;

    /**
     * 设置cookie的有效访问路径
     */
    @Value("${shiro.cookie.path}")
    private String path;

    /**
     * 设置HttpOnly属性
     */
    @Value("${shiro.cookie.httpOnly}")
    private boolean httpOnly;

    /**
     * 设置Cookie的过期时间，秒为单位
     */
    @Value("${shiro.cookie.maxAge}")
    private int maxAge;

    /**
     * 设置cipherKey密钥
     */
    @Value("${shiro.cookie.cipherKey}")
    private String cipherKey;

    /**
     * 登录地址
     */
    @Value("${shiro.user.loginUrl}")
    private String loginUrl;

    /**
     * 权限认证失败地址
     */
    @Value("${shiro.user.unauthorizedUrl}")
    private String unauthorizedUrl;

    /**
     * 是否开启记住我功能
     */
    @Getter
    @Value("${shiro.rememberMe.enabled:false}")
    private boolean rememberMe;

    /**
     * 额外定义的Shiro过滤链
     */
    @Value("#{'${shiro.filterChainDefinitions}'.empty ? null : '${shiro.filterChainDefinitions}'.split(',')}")
    private List<String> filterChainDefinitions = new ArrayList<>();

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        return defaultAdvisorAutoProxyCreator;
    }

    /**
     * Cache Manager (shiro-redis)
     */
    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        redisCacheManager.setPrincipalIdFieldName("userId");
        return redisCacheManager;
    }

    /**
     * RedisManager (shiro-redis)
     */
    @Bean
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(redisProperties.getHost() + ":" + redisProperties.getPort());
        redisManager.setDatabase(redisProperties.getDatabase());
        if (StringUtils.isNotEmpty(redisProperties.getPassword())) {
            redisManager.setPassword(redisProperties.getPassword());
        }
        redisManager.setTimeout(expireTime * 60);
        return redisManager;
    }

    /**
     * 自定义Realm
     */
    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        userRealm.setAuthorizationCacheName(Constants.SYS_AUTH_CACHE);
        userRealm.setCacheManager(redisCacheManager());
        return userRealm;
    }

    /**
     * RedisSessionDAO (shiro-redis)
     */
    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        redisSessionDAO.setExpire(expireTime * 60);
        return redisSessionDAO;
    }

    /**
     * 会话管理器
     */
    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager manager = new DefaultWebSessionManager();
        // 加入缓存管理器
        manager.setCacheManager(redisCacheManager());
        // 去掉 JSESSIONID
        manager.setSessionIdUrlRewritingEnabled(false);
        // 自定义SessionDao
        manager.setSessionDAO(redisSessionDAO());
        // 设置全局session超时时间
        manager.setGlobalSessionTimeout(expireTime * 60 * 1000L);
        return manager;
    }

    /**
     * 安全管理器
     */
    @Bean
    public SecurityManager securityManager(UserRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(userRealm);
        // 记住我
        securityManager.setRememberMeManager(rememberMe ? rememberMeManager() : null);
        // 注入缓存管理器;
        securityManager.setCacheManager(redisCacheManager());
        // session管理器
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    /**
     * 退出过滤器
     */
    public LogoutFilter logoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setLoginUrl(loginUrl);
        logoutFilter.setCacheManager(redisCacheManager());
        return logoutFilter;
    }

    /**
     * Shiro过滤器配置
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        CustomShiroFilterFactoryBean shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        // Shiro的核心安全接口,这个属性是必须的
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 身份认证失败，则跳转到登录页面的配置
        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        // 权限认证失败，则跳转到指定页面
        shiroFilterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        // Shiro连接约束配置，即过滤链的定义
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 对静态资源设置匿名访问
        filterChainDefinitionMap.put("/favicon.ico**", "anon");
        filterChainDefinitionMap.put("/ruoyi.png**", "anon");
        filterChainDefinitionMap.put("/html/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/docs/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/ajax/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/ruoyi/**", "anon");
        filterChainDefinitionMap.put("/captcha/captchaImage**", "anon");
        // 退出 logout地址，shiro去清除session
        filterChainDefinitionMap.put("/logout", "logout");
        // 不需要拦截的访问
        filterChainDefinitionMap.put("/login", "anon,captchaValidate");
        // 注册相关
        filterChainDefinitionMap.put("/register", "anon,captchaValidate");
        // 项目自定义过滤链的配置
        if (CollUtil.isNotEmpty(filterChainDefinitions)) {
            filterChainDefinitions.forEach(definition -> {
                String[] definitionMap = definition.split("=");
                filterChainDefinitionMap.put(definitionMap[0], definitionMap[1]);
            });
        }
        
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("captchaValidate", captchaValidateFilter());
        filters.put("kickout", kickoutSessionFilter());
        // 注销成功，则跳转到指定页面
        filters.put("logout", logoutFilter());
        shiroFilterFactoryBean.setFilters(filters);

        // 所有请求需要认证
        filterChainDefinitionMap.put("/**", "user,kickout");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }

    /**
     * 自定义验证码过滤器
     */
    public CaptchaValidateFilter captchaValidateFilter() {
        CaptchaValidateFilter captchaValidateFilter = new CaptchaValidateFilter();
        captchaValidateFilter.setCaptchaEnabled(captchaEnabled);
        captchaValidateFilter.setCaptchaType(captchaType);
        return captchaValidateFilter;
    }

    /**
     * cookie 属性设置
     */
    public SimpleCookie rememberMeCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge * 24 * 60 * 60);
        return cookie;
    }

    /**
     * 记住我
     */
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        if (StringUtils.isNotEmpty(cipherKey)) {
            cookieRememberMeManager.setCipherKey(Base64.decode(cipherKey));
        } else {
            cookieRememberMeManager.setCipherKey(CipherUtils.generateNewKey(128, "AES").getEncoded());
        }
        return cookieRememberMeManager;
    }

    /**
     * 同一个用户多设备登录限制
     */
    public KickoutSessionFilter kickoutSessionFilter() {
        KickoutSessionFilter kickoutSessionFilter = new KickoutSessionFilter();
        kickoutSessionFilter.setCacheManager(redisCacheManager());
        kickoutSessionFilter.setSessionManager(sessionManager());
        // 同一个用户最大的会话数，默认-1无限制；比如2的意思是同一个用户允许最多同时两个人登录
        kickoutSessionFilter.setMaxSession(maxSession);
        // 是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；踢出顺序
        kickoutSessionFilter.setKickoutAfter(kickoutAfter);
        // 被踢出后重定向到的地址；
        kickoutSessionFilter.setKickoutUrl("/login?kickout=1");
        return kickoutSessionFilter;
    }

    /**
     * thymeleaf模板引擎和shiro框架的整合
     */
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

    /**
     * 开启Shiro注解通知器
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}
