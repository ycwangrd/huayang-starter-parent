package com.huayang.product.framework.aspectj;

import com.huayang.product.common.core.context.PermissionContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 自定义权限拦截器，将权限字符串放到当前请求中以便用于多个角色匹配符合要求的权限
 *
 * @author huayang
 */
@Aspect
@Component
public class PermissionsAspect {
    @Before("@annotation(controllerRequiresPermissions)")
    public void doBefore(JoinPoint point, RequiresPermissions controllerRequiresPermissions) {
        handleRequiresPermissions(point, controllerRequiresPermissions);
    }

    protected void handleRequiresPermissions(final JoinPoint joinPoint, RequiresPermissions requiresPermissions) {
        PermissionContextHolder.setContext(StringUtils.join(requiresPermissions.value(), ","));
    }
}
