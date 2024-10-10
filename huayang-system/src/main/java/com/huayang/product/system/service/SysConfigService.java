package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.constant.Constants;
import com.huayang.product.common.constant.UserConstants;
import com.huayang.product.common.core.redis.RedisCache;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.exception.ServiceException;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.system.domain.SysConfig;
import com.huayang.product.system.mapper.SysConfigMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 参数配置 服务层实现
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> implements BaseService<SysConfig> {
    @Autowired
    private SysConfigMapper configMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init() {
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    public SysConfig selectById(Long configId) {
        return getById(configId);
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    public String selectByConfigKey(String configKey) {
        String configValue = ConvertUtil.toStr(redisCache.getCacheObject(getCacheKey(configKey)));
        if (StringUtils.isNotEmpty(configValue)) {
            return configValue;
        }
        LambdaQueryWrapper<SysConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig retConfig = getOne(queryWrapper);
        if (ObjUtil.isNotNull(retConfig)) {
            redisCache.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 查询参数配置列表
     *
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    public List<SysConfig> selectList(SysConfig config) {
        LambdaQueryWrapper<SysConfig> queryWrapper = Wrappers.lambdaQuery();
        if (config != null) {
            if (StringUtils.isNotEmpty(config.getConfigType())) {
                queryWrapper.eq(SysConfig::getConfigType, config.getConfigType());
            }
            if (StringUtils.isNotEmpty(config.getConfigName())) {
                queryWrapper.like(SysConfig::getConfigName, config.getConfigName());
            }
            if (StringUtils.isNotEmpty(config.getConfigKey())) {
                queryWrapper.like(SysConfig::getConfigKey, config.getConfigKey());
            }
            // 处理日期参数
            paramsTimeCondition(queryWrapper, SysConfig::getCreateTime, config.getParams());
        }
        return list(queryWrapper);
    }

    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertConfig(SysConfig config) {
        config.setCreateTime(DateUtils.getNowDate());
        boolean result = save(config);
        if (result) {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return result;
    }

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(SysConfig config) {
        SysConfig temp = getById(config.getConfigId());
        if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey())) {
            redisCache.deleteObject(getCacheKey(temp.getConfigKey()));
        }
        boolean result = updateById(config);
        if (result) {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return result;
    }

    /**
     * 批量删除参数配置对象
     *
     * @param ids 需要删除的数据ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(String ids) {
        Long[] configIds = ConvertUtil.toLongArray(ids);
        for (Long configId : configIds) {
            SysConfig config = selectById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType())) {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            removeById(configId);
            redisCache.deleteObject(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    public void loadingConfigCache() {
        List<SysConfig> configsList = list();
        for (SysConfig config : configsList) {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    public void clearConfigCache() {
        Collection<String> keys = redisCache.keys(Constants.SYS_CONFIG_KEY + "*");
        redisCache.deleteObject(keys);
    }

    /**
     * 重置参数缓存数据
     */
    public void resetConfigCache() {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数配置信息
     * @return 结果
     */
    public boolean checkConfigKeyUnique(SysConfig config) {
        Long configId = config.getConfigId() == null ? -1L : config.getConfigId();
        LambdaQueryWrapper<SysConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
        queryWrapper.ne(SysConfig::getConfigId, configId);
        return CollUtil.isEmpty(list(queryWrapper)) ? UserConstants.UNIQUE : UserConstants.NOT_UNIQUE;
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey) {
        return Constants.SYS_CONFIG_KEY + configKey;
    }
}
