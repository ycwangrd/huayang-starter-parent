package com.huayang.product.common.xss;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 防止XSS攻击的过滤器
 *
 * @author huayang
 */
@Slf4j
public class XssFilter implements Filter {
    /**
     * 排除链接
     */
    public List<String> excludes = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String tempExcludes = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(tempExcludes)) {
            String[] urls = tempExcludes.split(",");
            excludes.addAll(Arrays.asList(urls));
        }
        log.info("XSS过滤器排除路径:{}", excludes);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || method.matches("GET") || method.matches("DELETE")) {
            return true;
        }
        // 排除的链接
        return matches(url, excludes);
    }

    @Override
    public void destroy() {

    }

    /**
     * 判断url是否匹配
     *
     * @param url
     * @param strs
     * @return
     */
    private boolean matches(String url, List<String> strs) {
        if (StrUtil.isEmpty(url) || CollUtil.isEmpty(strs)) {
            return false;
        }
        for (String pattern : strs) {
            AntPathMatcher matcher = new AntPathMatcher();
            return matcher.match(pattern, url);
        }
        return false;
    }
}