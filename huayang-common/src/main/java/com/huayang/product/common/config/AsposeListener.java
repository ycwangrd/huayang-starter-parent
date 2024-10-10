package com.huayang.product.common.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * @author huayang
 * @version 1.0
 * @desc aspose序列号加载
 * @since 2023年01月18日 星期三 16:17
 */
@Slf4j
@WebListener
public class AsposeListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        try {
            // Aspose的序列号设置
            final long startTimeMillis = System.currentTimeMillis();
            log.info("开始执行Aspose序列号注册...");
            // 此方式读取文件打jar包会遇到问题
            // ClassPathResource licFile = new ClassPathResource("Aspose.Total.Java.lic");
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource licFile = resourceLoader.getResource("Aspose.Total.Java.lic");
            new com.aspose.cells.License().setLicense(licFile.getInputStream());
            log.info("Aspose Cells 序列号注册成功..");
            new com.aspose.words.License().setLicense(licFile.getInputStream());
            log.info("Aspose Words 序列号注册成功..");
            log.info("Aspose序列号注册耗时[{}]毫秒.", System.currentTimeMillis() - startTimeMillis);
        } catch (Exception e) {
            log.error("Aspose License 初始化失败!!!", e);
        }
        // Application中设置上下文
        context.setAttribute("ctx", context.getContextPath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("AsposeListener:Servlet容器被销毁了");
    }
}
