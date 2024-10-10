package com.huayang.product.quartz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.quartz.domain.SysJobLog;
import com.huayang.product.quartz.mapper.SysJobLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 定时任务调度日志信息信息 服务层
 *
 * @author huayang
 */
@Service
@Transactional(readOnly = true)
public class SysJobLogService extends ServiceImpl<SysJobLogMapper, SysJobLog> implements BaseService<SysJobLog> {
    private static final String CACHE_NAME = "sysJobLog";

    /**
     * 获取quartz调度器日志的计划任务
     *
     * @param jobLog 调度日志信息
     * @return 调度任务日志集合
     */
    public List<SysJobLog> selectList(SysJobLog jobLog) {
        LambdaQueryWrapper<SysJobLog> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(jobLog.getJobName())) {
            queryWrapper.like(SysJobLog::getJobName, jobLog.getJobName());
        }
        if (StringUtils.isNotBlank(jobLog.getJobGroup())) {
            queryWrapper.eq(SysJobLog::getJobGroup, jobLog.getJobGroup());
        }
        if (StringUtils.isNotBlank(jobLog.getStatus())) {
            queryWrapper.eq(SysJobLog::getStatus, jobLog.getStatus());
        }
        if (StringUtils.isNotBlank(jobLog.getInvokeTarget())) {
            queryWrapper.like(SysJobLog::getInvokeTarget, jobLog.getInvokeTarget());
        }
        // 时间查询参数
        paramsTimeCondition(queryWrapper, SysJobLog::getCreateTime, jobLog.getParams());
        // 排序
        queryWrapper.orderByDesc(SysJobLog::getCreateTime);
        return list(queryWrapper);
    }

    /**
     * 新增任务日志
     *
     * @param jobLog 调度日志信息
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void addJobLog(SysJobLog jobLog) {
        jobLog.setCreateTime(DateUtils.getNowDate());
        save(jobLog);
    }

    /**
     * 获取指定任务日志
     *
     * @param jobLogId
     * @return
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == null")
    public SysJobLog selectById(Long jobLogId) {
        return getById(jobLogId);
    }

    /**
     * 批量删除调度日志信息
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
     * 清空任务日志
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void cleanJobLog() {
        baseMapper.cleanJobLog();
    }
}
