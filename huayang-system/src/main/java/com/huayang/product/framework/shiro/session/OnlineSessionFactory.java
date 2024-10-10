package com.huayang.product.framework.shiro.session;

import com.huayang.product.common.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionFactory;
import org.apache.shiro.web.session.mgt.WebSessionContext;
import org.dromara.hutool.http.useragent.UserAgent;
import org.dromara.hutool.http.useragent.UserAgentUtil;
import org.springframework.stereotype.Component;

/**
 * 自定义sessionFactory会话
 *
 * @author huayang
 */
@Component
public class OnlineSessionFactory implements SessionFactory {
    @Override
    public Session createSession(SessionContext initData) {
        OnlineSession session = new OnlineSession();
        if (initData != null && initData instanceof WebSessionContext) {
            WebSessionContext sessionContext = (WebSessionContext) initData;
            HttpServletRequest request = (HttpServletRequest) sessionContext.getServletRequest();
            if (request != null) {
                UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
                // 获取客户端操作系统
                String os = userAgent.getOs().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                session.setHost(IpUtils.getIpAddr(request));
                session.setBrowser(browser);
                session.setOs(os);
            }
        }
        return session;
    }
}
