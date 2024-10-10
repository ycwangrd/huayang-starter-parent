package com.huayang.product.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 业务异常
 *
 * @author huayang
 */
@Getter
@NoArgsConstructor
public final class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     * <p>
     */
    private String detailMessage;

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }
}