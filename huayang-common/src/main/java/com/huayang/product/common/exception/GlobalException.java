package com.huayang.product.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 全局异常
 *
 * @author huayang
 */
@Getter
@NoArgsConstructor
public class GlobalException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     * <p>
     * 和 {@link CommonResult#getDetailMessage()} 一致的设计
     */
    private String detailMessage;

    public GlobalException(String message) {
        this.message = message;
    }

    public GlobalException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    public GlobalException setMessage(String message) {
        this.message = message;
        return this;
    }
}