package com.huayang.product.framework.config;

import com.huayang.product.common.config.HmcConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangrd
 * @version 1.0
 * @date 2024年09月14日 星期六 10:39
 * @desc Swagger配置类
 */
@Slf4j
@Configuration
@ConditionalOnClass(value = {org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class})
public class SpringDocConfig {
    @Bean
    public OpenAPI openApi() {
        // 配置文档基本信息
        Info info = new Info()
                // 文档标题
                .title(HmcConfig.getName() + "API文档")
                // 摘要信息
                .summary("摘要信息")
                .termsOfService("https://www.huayangmaritime.com.cn/")
                // 描述信息
                .description("项目描述信息")
                // 文档作者
                .contact(
                        new Contact().name("HUAYANG")
                                .url("https://www.huayangmaritime.com.  cn/")
                )
                // License许可证信息
                .license(
                        new License().name("Apache 2.0")
                                .url("https://www.huayangmaritime.com.cn/")
                )
                // 文档版本
                .version("1.0.0");
        return new OpenAPI().info(info);
    }
}
