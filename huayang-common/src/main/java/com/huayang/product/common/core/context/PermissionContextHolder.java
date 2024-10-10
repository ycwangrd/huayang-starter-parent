package com.huayang.product.common.core.context;

import org.dromara.hutool.core.convert.ConvertUtil;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 权限信息
 *
 * @author huayang
 */
public class PermissionContextHolder {
    private static final String PERMISSION_CONTEXT_ATTRIBUTES = "PERMISSION_CONTEXT";

    public static String getContext() {
        return ConvertUtil.toStr(RequestContextHolder.currentRequestAttributes()
                .getAttribute(PERMISSION_CONTEXT_ATTRIBUTES, RequestAttributes.SCOPE_REQUEST));
    }

    public static void setContext(String permission) {
        RequestContextHolder.currentRequestAttributes()
                .setAttribute(PERMISSION_CONTEXT_ATTRIBUTES, permission, RequestAttributes.SCOPE_REQUEST);
    }
}
