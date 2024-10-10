package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.system.domain.SysOperLog;
import com.huayang.product.system.mapper.SysOperLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 操作日志 服务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysOperLogService extends ServiceImpl<SysOperLogMapper, SysOperLog> implements BaseService<SysOperLog> {
    private static final String CACHE_NAME = "sysOperLog";

    /**
     * 查询系统操作日志集合
     *
     * @param entity 操作日志对象
     * @return 操作日志集合
     */
    public List<SysOperLog> selectList(SysOperLog entity) {
        LambdaQueryWrapper<SysOperLog> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(entity.getOperIp())) {
            queryWrapper.eq(SysOperLog::getOperIp, entity.getOperIp());
        }
        if (StringUtils.isNotBlank(entity.getTitle())) {
            queryWrapper.like(SysOperLog::getTitle, entity.getTitle());
        }
        if (entity.getBusinessType() != null) {
            queryWrapper.eq(SysOperLog::getBusinessType, entity.getBusinessType());
        }
        if (ArrayUtil.isNotEmpty(entity.getBusinessTypes())) {
            queryWrapper.in(SysOperLog::getBusinessType, entity.getBusinessTypes());
        }
        if (entity.getStatus() != null) {
            queryWrapper.eq(SysOperLog::getStatus, entity.getStatus());
        }
        if (StringUtils.isNotBlank(entity.getOperName())) {
            queryWrapper.like(SysOperLog::getOperName, entity.getOperName());
        }
        // 时间查询参数
        paramsTimeCondition(queryWrapper, SysOperLog::getOperTime, entity.getParams());
        return list(queryWrapper);
    }

    /**
     * 清空操作日志
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void cleanOperLog() {
        baseMapper.cleanOperLog();
    }

    /**
     * 通过id查询操作日志明细
     *
     * @param operId
     * @return
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == null")
    public SysOperLog selectById(Long operId) {
        return getById(operId);
    }

    /**
     * 删除操作日志
     *
     * @param ids
     * @return
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(String ids) {
        return removeByIds(Arrays.asList(ConvertUtil.toLongArray(ids)));
    }

    /**
     * 保存操作日志
     *
     * @param operLog
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void insertOperlog(SysOperLog operLog) {
        save(operLog);
    }
}
