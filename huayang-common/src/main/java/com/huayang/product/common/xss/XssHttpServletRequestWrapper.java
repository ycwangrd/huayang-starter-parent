package com.huayang.product.common.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.dromara.hutool.http.html.HtmlFilter;

/**
 * XSS过滤处理
 *
 * @author huayang
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * @param request
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapseValues = new String[length];
            for (int i = 0; i < length; i++) {
                // 防xss攻击和过滤前后空格
                escapseValues[i] = new HtmlFilter().filter(values[i]).trim();
            }
            return escapseValues;
        }
        return super.getParameterValues(name);
    }
}