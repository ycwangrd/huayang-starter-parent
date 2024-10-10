package com.huayang.product.framework.web.global;

import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.date.DateUtil;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.beans.PropertyEditorSupport;
import java.util.Date;

/**
 * @author wangrd
 * @version 1.0
 * @date 2024年08月07日 星期三 17:50
 * @desc
 */
@Slf4j
@Configuration
public class GlobalControllerConfig {

    @Bean
    public RequestMappingHandlerAdapter webBindingInitializer(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        requestMappingHandlerAdapter.setWebBindingInitializer(binder -> {
            // 设置需要参数的元素个数，默认为256
            binder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
            // 将前台传递过来的日期格式的字符串，自动转化为Date类型
            binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
                @Override
                public void setAsText(String text) {
                    // DateUtil.parse是hutool当中的方法
                    setValue(DateUtil.parse(text));
                }
            });
            // 如果是字符串类型，就去除字符串的前后空格
            binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        });
        return requestMappingHandlerAdapter;
    }
}
