package com.huayang.product.web.controller.monitor;

import com.huayang.product.common.annotation.Log;
import com.huayang.product.common.constant.ShiroConstants;
import com.huayang.product.common.core.controller.BaseController;
import com.huayang.product.common.core.domain.AjaxResult;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.core.page.TableDataInfo;
import com.huayang.product.common.enums.BusinessType;
import com.huayang.product.common.utils.ShiroUtils;
import com.huayang.product.system.domain.SysUserOnline;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.*;

/**
 * 在线用户监控
 *
 * @author huayang
 */
@Controller
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController {
    private String prefix = "monitor/online";

    @Autowired
    private RedisSessionDAO redisSessionDAO;


    @RequiresPermissions("monitor:online:view")
    @GetMapping()
    public String online() {
        return prefix + "/online";
    }

    @RequiresPermissions("monitor:online:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysUserOnline userOnline) {
        String ipaddr = userOnline.getIpaddr();
        String loginName = userOnline.getLoginName();
        TableDataInfo rspData = new TableDataInfo();
        Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        Iterator<Session> it = sessions.iterator();
        List<SysUserOnline> sessionList = new ArrayList<>();
        while (it.hasNext()) {
            SysUserOnline user = getSession(it.next());
            if (ObjUtil.isNotNull(user)) {
                if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(loginName)) {
                    if (StringUtils.equals(ipaddr, user.getIpaddr())
                            && StringUtils.equals(loginName, user.getLoginName())) {
                        sessionList.add(user);
                    }
                } else if (StringUtils.isNotEmpty(ipaddr)) {
                    if (StringUtils.equals(ipaddr, user.getIpaddr())) {
                        sessionList.add(user);
                    }
                } else if (StringUtils.isNotEmpty(loginName)) {
                    if (StringUtils.equals(loginName, user.getLoginName())) {
                        sessionList.add(user);
                    }
                } else {
                    sessionList.add(user);
                }
            }
        }
        rspData.setRows(sessionList);
        rspData.setTotal(sessionList.size());
        return rspData;
    }

    @RequiresPermissions(value = {"monitor:online:batchForceLogout", "monitor:online:forceLogout"}, logical = Logical.OR)
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @PostMapping("/batchForceLogout")
    @ResponseBody
    public AjaxResult batchForceLogout(@RequestBody List<SysUserOnline> sysUserOnlines) {
        for (SysUserOnline userOnline : sysUserOnlines) {
            String sessionId = userOnline.getSessionId();
            String loginName = userOnline.getLoginName();
            if (sessionId.equals(ShiroUtils.getSessionId())) {
                return error("当前登录用户无法强退");
            }
            redisSessionDAO.delete(redisSessionDAO.readSession(sessionId));
            removeUserCache(loginName, sessionId);
        }
        return success();
    }

    private SysUserOnline getSession(Session session) {
        Object obj = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
        if (null == obj) {
            return null;
        }
        if (obj instanceof SimplePrincipalCollection) {
            SimplePrincipalCollection spc = (SimplePrincipalCollection) obj;
            obj = spc.getPrimaryPrincipal();
            if (null != obj && obj instanceof SysUser) {
                SysUser sysUser = (SysUser) obj;
                SysUserOnline userOnline = new SysUserOnline();
                userOnline.setSessionId(session.getId().toString());
                userOnline.setLoginName(sysUser.getLoginName());
                if (ObjUtil.isNotNull(sysUser.getDept()) && StringUtils.isNotEmpty(sysUser.getDept().getDeptName())) {
                    userOnline.setDeptName(sysUser.getDept().getDeptName());
                }
                userOnline.setIpaddr(session.getHost());
                userOnline.setStartTimestamp(session.getStartTimestamp());
                userOnline.setLastAccessTime(session.getLastAccessTime());
                return userOnline;
            }
        }
        return null;
    }

    public void removeUserCache(String loginName, String sessionId) {
        Cache<String, Deque<Serializable>> cache = SpringUtil.getBean(RedisCacheManager.class).getCache(ShiroConstants.SYS_USERCACHE);
        Deque<Serializable> deque = cache.get(loginName);
        if (CollUtil.isEmpty(deque)) {
            return;
        }
        deque.remove(sessionId);
        cache.put(loginName, deque);
    }
}
