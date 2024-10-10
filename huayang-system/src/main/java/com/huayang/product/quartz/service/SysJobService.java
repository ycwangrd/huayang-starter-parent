package com.huayang.product.quartz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.constant.ScheduleConstants;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.exception.job.TaskException;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.quartz.domain.SysJob;
import com.huayang.product.quartz.mapper.SysJobMapper;
import com.huayang.product.quartz.util.CronUtils;
import com.huayang.product.quartz.util.ScheduleUtils;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时任务调度信息信息 服务层
 *
 * @author huayang
 */
@Service
@Transactional(readOnly = true)
public class SysJobService extends ServiceImpl<SysJobMapper, SysJob> implements BaseService<SysJob> {
    @Autowired
    private Scheduler scheduler;

    /**
     * 项目启动时，初始化定时器
     * 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
     */
    @PostConstruct
    public void init() throws SchedulerException, TaskException {
        scheduler.clear();
        List<SysJob> jobList = list();
        for (SysJob job : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    /**
     * 获取quartz调度器的计划任务列表
     *
     * @param job 调度信息
     * @return
     */
    public List<SysJob> selectJobList(SysJob job) {
        LambdaQueryWrapper<SysJob> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotEmpty(job.getJobName())) {
            queryWrapper.like(SysJob::getJobName, job.getJobName());
        }
        if (StringUtils.isNotEmpty(job.getJobGroup())) {
            queryWrapper.like(SysJob::getJobGroup, job.getJobGroup());
        }
        if (StringUtils.isNotEmpty(job.getStatus())) {
            queryWrapper.eq(SysJob::getStatus, job.getStatus());
        }
        if (StringUtils.isNotEmpty(job.getInvokeTarget())) {
            queryWrapper.like(SysJob::getInvokeTarget, job.getInvokeTarget());
        }
        return list(queryWrapper);
    }

    /**
     * 暂停任务
     *
     * @param job 调度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        job.setUpdateTime(DateUtils.getNowDate());
        boolean result = updateById(job);
        if (result) {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return result;
    }

    /**
     * 恢复任务
     *
     * @param job 调度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.NORMAL.getValue());
        job.setUpdateTime(DateUtils.getNowDate());
        boolean result = updateById(job);
        if (result) {
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return result;
    }

    /**
     * 删除任务后，所对应的trigger也将被删除
     *
     * @param job 调度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        boolean result = removeById(jobId);
        if (result) {
            scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return result;
    }

    /**
     * 批量删除调度信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobByIds(String ids) throws SchedulerException {
        Long[] jobIds = ConvertUtil.toLongArray(ids);
        for (Long jobId : jobIds) {
            SysJob job = getById(jobId);
            deleteJob(job);
        }
    }

    /**
     * 任务调度状态修改
     *
     * @param job 调度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(SysJob job) throws SchedulerException {
        boolean result = false;
        String status = job.getStatus();
        if (ScheduleConstants.Status.NORMAL.getValue().equals(status)) {
            result = resumeJob(job);
        } else if (ScheduleConstants.Status.PAUSE.getValue().equals(status)) {
            result = pauseJob(job);
        }
        return result;
    }

    /**
     * 立即运行任务
     *
     * @param job 调度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean run(SysJob job) throws SchedulerException {
        boolean result = false;
        Long jobId = job.getJobId();
        SysJob tmpObj = getById(jobId);
        // 参数
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ScheduleConstants.TASK_PROPERTIES, tmpObj);
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, tmpObj.getJobGroup());
        if (scheduler.checkExists(jobKey)) {
            result = true;
            scheduler.triggerJob(jobKey, dataMap);
        }
        return result;
    }

    /**
     * 新增任务
     *
     * @param job 调度信息 调度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertJob(SysJob job) throws SchedulerException, TaskException {
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        job.setCreateTime(DateUtils.getNowDate());
        boolean result = save(job);
        if (result) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return result;
    }

    /**
     * 通过id查询明细
     *
     * @param jobId
     * @return
     */
    public SysJob selectById(Long jobId) {
        return getById(jobId);
    }

    /**
     * 更新任务的时间表达式
     *
     * @param job 调度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(SysJob job) throws SchedulerException, TaskException {
        SysJob properties = getById(job.getJobId());
        job.setUpdateTime(DateUtils.getNowDate());
        boolean result = updateById(job);
        if (result) {
            updateSchedulerJob(job, properties.getJobGroup());
        }
        return result;
    }

    /**
     * 更新任务
     *
     * @param job      任务对象
     * @param jobGroup 任务组名
     */
    public void updateSchedulerJob(SysJob job, String jobGroup) throws SchedulerException, TaskException {
        Long jobId = job.getJobId();
        // 判断是否存在
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(jobKey);
        }
        ScheduleUtils.createScheduleJob(scheduler, job);
    }

    /**
     * 校验cron表达式是否有效
     *
     * @param cronExpression 表达式
     * @return 结果
     */
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtils.isValid(cronExpression);
    }
}