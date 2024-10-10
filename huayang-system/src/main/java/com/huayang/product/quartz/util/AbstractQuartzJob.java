package com.huayang.product.quartz.util;

import com.huayang.product.common.constant.Constants;
import com.huayang.product.common.constant.ScheduleConstants;
import com.huayang.product.common.utils.ExceptionUtil;
import com.huayang.product.common.utils.bean.BeanUtils;
import com.huayang.product.quartz.domain.SysJob;
import com.huayang.product.quartz.domain.SysJobLog;
import com.huayang.product.quartz.service.SysJobLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * 抽象quartz调用
 *
 * @author huayang
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job {

    /**
     * 线程本地变量
     */
    private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SysJob sysJob = new SysJob();
        BeanUtils.copyBeanProp(sysJob, context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES));
        try {
            before(context, sysJob);
            if (sysJob != null) {
                doExecute(context, sysJob);
            }
            after(context, sysJob, null);
        } catch (Exception e) {
            log.error("任务执行异常  - ：", e);
            after(context, sysJob, e);
        }
    }

    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     */
    protected void before(JobExecutionContext context, SysJob sysJob) {
        threadLocal.set(new Date());
    }

    /**
     * 执行后
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     */
    protected void after(JobExecutionContext context, SysJob sysJob, Exception e) {
        Date startTime = threadLocal.get();
        threadLocal.remove();

        final SysJobLog sysJobLog = new SysJobLog();
        sysJobLog.setJobName(sysJob.getJobName());
        sysJobLog.setJobGroup(sysJob.getJobGroup());
        sysJobLog.setInvokeTarget(sysJob.getInvokeTarget());
        sysJobLog.setStartTime(startTime);
        sysJobLog.setEndTime(new Date());
        long runMs = sysJobLog.getEndTime().getTime() - sysJobLog.getStartTime().getTime();
        sysJobLog.setJobMessage(sysJobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
        if (e != null) {
            sysJobLog.setStatus(Constants.FAIL);
            String errorMsg = StringUtils.substring(ExceptionUtil.getExceptionMessage(e), 0, 2000);
            sysJobLog.setExceptionInfo(errorMsg);
        } else {
            sysJobLog.setStatus(Constants.SUCCESS);
        }

        // 写入数据库当中
        SpringUtil.getBean(SysJobLogService.class).addJobLog(sysJobLog);
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;
}
