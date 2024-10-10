package com.huayang.product.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.crypto.digest.DigestUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis配置
 *
 * @author huayang
 */
@Slf4j
/** 开启缓存支持 */
@EnableCaching
@Configuration
public class RedisConfig implements CachingConfigurer {
    /**
     * 自定义缓存key生成策略
     *
     * @return
     */
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder builder = new StringBuilder();
            // 获取类名
            builder.append(target.getClass().getName());
            builder.append(".");
            // 获取方法名
            builder.append(method.getName());
            // 参数对象转md5
            if (ArrayUtil.isNotEmpty(objects)) {
                StringBuilder paramBuilder = new StringBuilder();
                for (Object obj : objects) {
                    paramBuilder.append("::");
                    paramBuilder.append(obj);
                }
                builder.append("::" + DigestUtil.md5Hex(paramBuilder.toString()));
            }
            return builder.toString();
        };
    }

    /**
     * 自定义缓存异常处理
     *
     * @return
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("Get: {} from cache failed", key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Put: {} into cache failed", key, value, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Evict: {} from cache failed", key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Clear: {} from cache failed", cache, exception);
            }
        };
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private String limitScriptText() {
        return "local key = KEYS[1]\n" +
                "local count = tonumber(ARGV[1])\n" +
                "local time = tonumber(ARGV[2])\n" +
                "local current = redis.call('get', key);\n" +
                "if current and tonumber(current) > count then\n" +
                "    return tonumber(current);\n" +
                "end\n" +
                "current = redis.call('incr', key)\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call('expire', key, time)\n" +
                "end\n" +
                "return tonumber(current);";
    }
}
