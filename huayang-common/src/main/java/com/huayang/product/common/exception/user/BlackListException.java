package com.huayang.product.common.exception.user;

/**
 * 黑名单IP异常类
 *
 * @author huayang
 */
public class BlackListException extends UserException {
    private static final long serialVersionUID = 1L;

    public BlackListException() {
        super("login.blocked", null);
    }
}
