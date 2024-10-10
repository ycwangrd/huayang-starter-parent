package com.huayang.product.framework.web.service;

import com.huayang.product.system.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RuoYi首创 html调用 thymeleaf 实现参数管理
 *
 * @author huayang
 */
@Service("config")
public class ConfigService {
    @Autowired
    private SysConfigService configService;

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数键名
     * @return 参数键值
     */
    public String getKey(String configKey) {
        return configService.selectByConfigKey(configKey);
    }
}
