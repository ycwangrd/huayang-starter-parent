package com.huayang.product.common.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * 全局配置类
 *
 * @author huayang
 */
@Component
@ConfigurationProperties(prefix = "hmc")
public class HmcConfig {
    /**
     * 项目名称
     */
    @Getter
    private static String name;

    /**
     * 版本
     */
    @Getter
    private static String version;

    /**
     * 版权年份
     */
    @Getter
    private static String copyrightYear = Year.now().toString();

    /**
     * 实例演示开关
     */
    @Getter
    private static boolean demoEnabled;

    /**
     * 上传路径
     */
    @Getter
    private static String profile;

    /**
     * 获取地址开关
     */
    @Getter
    private static boolean addressEnabled;

    /**
     * 获取导入上传路径
     */
    public static String getImportPath() {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }

    public void setName(String name) {
        HmcConfig.name = name;
    }

    public void setVersion(String version) {
        HmcConfig.version = version;
    }

    public void setDemoEnabled(boolean demoEnabled) {
        HmcConfig.demoEnabled = demoEnabled;
    }

    public void setProfile(String profile) {
        HmcConfig.profile = profile;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        HmcConfig.addressEnabled = addressEnabled;
    }
}
