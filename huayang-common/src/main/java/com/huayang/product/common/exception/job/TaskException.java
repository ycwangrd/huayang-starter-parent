package com.huayang.product.common.exception.job;

import lombok.Getter;

/**
 * 计划策略异常
 *
 * @author huayang
 */
public class TaskException extends Exception {
    private static final long serialVersionUID = 1L;

    @Getter
    private Code code;

    public TaskException(String msg, Code code) {
        this(msg, code, null);
    }

    public TaskException(String msg, Code code, Exception nestedEx) {
        super(msg, nestedEx);
        this.code = code;
    }

    public enum Code {
        TASK_EXISTS, NO_TASK_EXISTS, TASK_ALREADY_STARTED, UNKNOWN, CONFIG_ERROR, TASK_NODE_NOT_AVAILABLE
    }
}