package com.huayang.product.quartz.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.huayang.product.common.annotation.Excel;
import lombok.Data;
import org.dromara.hutool.core.date.DatePattern;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务调度日志表 sys_job_log
 *
 * @author huayang
 */
@Data
@TableName("sys_job_log")
public class SysJobLog {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    @Excel(name = "日志序号")
    private Long jobLogId;

    /**
     * 任务名称
     */
    @Excel(name = "任务名称")
    private String jobName;

    /**
     * 任务组名
     */
    @Excel(name = "任务组名")
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    @Excel(name = "调用目标字符串")
    private String invokeTarget;

    /**
     * 日志信息
     */
    @Excel(name = "日志信息")
    private String jobMessage;

    /**
     * 执行状态（0正常 1失败）
     */
    @Excel(name = "执行状态", readConverterExp = "0=正常,1=失败")
    private String status;

    /**
     * 异常信息
     */
    @Excel(name = "异常信息")
    private String exceptionInfo;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date createTime;

    /**
     * 开始时间
     */
    @TableField(exist = false)
    private Date startTime;

    /**
     * 结束时间
     */
    @TableField(exist = false)
    private Date endTime;

    /**
     * 请求参数
     */
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params = new HashMap<>();
}
