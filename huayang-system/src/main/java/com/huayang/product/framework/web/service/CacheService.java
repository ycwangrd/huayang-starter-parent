package com.huayang.product.framework.web.service;

import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.core.redis.RedisCache;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.json.JSONObject;
import jakarta.annotation.PostConstruct;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.crazycake.shiro.IRedisManager;
import org.crazycake.shiro.exception.SerializationException;
import org.crazycake.shiro.serializer.ObjectSerializer;
import org.crazycake.shiro.serializer.StringSerializer;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.reflect.FieldUtil;
import org.dromara.hutool.core.reflect.ReflectUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 缓存操作处理
 *
 * @author ruoyi
 */
@Service
public class CacheService {
    /**
     * shiro-redis 默认会话前缀
     */
    private final String DEFAULT_SESSION_KEY_PREFIX = "shiro:session:";
    private final String DEFAULT_AUTHCACHE_KEY_PREFIX = "shiro:cache:sys-authCache";
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IRedisManager redisManager;
    /**
     * 缓存名称列表
     */
    private String[] cacheNames = {};

    /**
     * 初始化缓存名称列表
     */
    @PostConstruct
    public void init() {
        getCacheNames();
    }

    /**
     * 获取所有缓存名称
     * 通过反射获取实现了接口BaseService的类的CACHE_NAME变量定义的缓存名称
     *
     * @return 缓存列表
     */
    public String[] getCacheNames() {
        if (ArrayUtil.isEmpty(cacheNames)) {
            cacheNames = new String[]{"shiro:session", "shiro:cache:sys-authCache", "shiro:cache:sys-userCache", "sys_dict", "sys_config", "sys_loginRecordCache"};
            Map<String, BaseService> serviceList = SpringUtil.getBeansOfType(BaseService.class);
            for (BaseService impl : serviceList.values()) {
                try {
                    Field cacheName = FieldUtil.getField(impl.getClass(), "CACHE_NAME");
                    if (cacheName != null) {
                        ReflectUtil.setAccessible(cacheName);
                        cacheNames = ArrayUtil.append(cacheNames, String.valueOf(cacheName.get(impl)));
                    }
                } catch (Exception e) {
                }
            }
        }
        return cacheNames;
    }

    /**
     * 根据缓存名称获取所有键名
     *
     * @param cacheName 缓存名称
     * @return 键名列表
     */
    public Set<String> getCacheKeys(String cacheName) {
        Set<String> tmpKeys = new HashSet<>();
        Collection<String> cacheKeys = redisCache.keys(cacheName + ":*");
        for (String cacheKey : cacheKeys) {
            tmpKeys.add(cacheKey);
        }
        return new TreeSet<>(tmpKeys);
    }

    /**
     * 根据缓存名称和键名获取内容值
     *
     * @param cacheName 缓存名称
     * @param cacheKey  键名
     * @return 键值
     */
    public Object getCacheValue(String cacheName, String cacheKey) {
        if (cacheKey.contains(DEFAULT_SESSION_KEY_PREFIX)) {
            try {
                SimpleSession simpleSession = (SimpleSession) new ObjectSerializer().deserialize(redisManager.get(new StringSerializer().serialize(cacheKey)));
                Object obj = simpleSession.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                if (null == obj) {
                    return "未登录会话";
                }
                if (obj instanceof SimplePrincipalCollection) {
                    SimplePrincipalCollection spc = (SimplePrincipalCollection) obj;
                    obj = spc.getPrimaryPrincipal();
                    if (null != obj && obj instanceof SysUser) {
                        SysUser sysUser = (SysUser) obj;
                        return sysUser;
                    }
                }
                return obj;
            } catch (SerializationException e) {
                return null;
            }
        }
        if (cacheKey.contains(DEFAULT_AUTHCACHE_KEY_PREFIX)) {
            try {
                SimpleAuthorizationInfo simpleAuthorizationInfo = (SimpleAuthorizationInfo) new ObjectSerializer().deserialize(redisManager.get(new StringSerializer().serialize(cacheKey)));
                JSONObject authJson = new JSONObject();
                if (ObjUtil.isNotNull(simpleAuthorizationInfo)) {
                    authJson.put("roles", simpleAuthorizationInfo.getRoles());
                    authJson.put("permissions", simpleAuthorizationInfo.getStringPermissions());
                }
                return authJson;
            } catch (SerializationException e) {
                return null;
            }
        }
        return redisCache.getCacheObject(cacheKey);
    }

    /**
     * 根据名称删除缓存信息
     *
     * @param cacheName 缓存名称
     */
    public void clearCacheName(String cacheName) {
        Collection<String> cacheKeys = redisCache.keys(cacheName + ":*");
        redisCache.deleteObject(cacheKeys);
    }

    /**
     * 根据名称和键名删除缓存信息
     *
     * @param cacheName 缓存名称
     * @param cacheKey  键名
     */
    public void clearCacheKey(String cacheName, String cacheKey) {
        redisCache.deleteObject(cacheKey);
    }

    /**
     * 清理所有缓存
     */
    public void clearAll() {
        Collection<String> cacheKeys = redisCache.keys("*");
        redisCache.deleteObject(cacheKeys);
    }
}
