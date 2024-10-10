package com.huayang.product.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.huayang.product.common.enums.OnlineStatus;
import lombok.Data;
import org.dromara.hutool.core.date.DatePattern;

import java.util.Date;

/**
 * 当前在线会话 sys_user_online
 *
 * @author huayang
 */
@Data
@TableName("sys_user_online")
public class SysUserOnline {
    private static final long serialVersionUID = 1L;

    /**
     * 用户会话id
     */
    @TableId
    private String sessionId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 登录名称
     */
    private String loginName;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地址
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * session创建时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date startTimestamp;

    /**
     * session最后访问时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date lastAccessTime;

    /**
     * 超时时间，单位为毫秒
     */
    private Long expireTime;

    /**
     * 在线状态
     */
    private OnlineStatus status = OnlineStatus.on_line;
}
