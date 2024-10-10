package com.huayang;

import com.huayang.product.common.config.HmcConfig;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 启动程序
 * 此启动类必须在com.huayang包下,因为产品的是com.huayang.product,项目的包名com.huayang.project,需要全部扫描到
 * @author huayang
 */
// 表示通过aop框架暴露该代理对象,AopContext能够访问
@EnableAspectJAutoProxy(exposeProxy = true)
/** 开启异步执行支持 */
@EnableAsync
/** 开启定时任务支持 */
@EnableScheduling
/** 开启事务支持 */
@EnableTransactionManagement
/** 自定义Servlet、Filter、Listener的支持 */
@ServletComponentScan
@SpringBootApplication
public class StarterApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
        System.out.println("********************************************************************");
        System.out.println(StrUtil.format("******(♥◠‿◠)ﾉﾞ  {}-项目启动成功   ლ(´ڡ`ლ)ﾞ******", HmcConfig.getName()));
        System.out.println("********************************************************************");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(StarterApplication.class);
    }
}