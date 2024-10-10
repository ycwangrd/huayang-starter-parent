package com.huayang.product.web.controller.system;

import com.huayang.product.common.core.controller.BaseController;
import com.huayang.product.common.core.domain.AjaxResult;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.framework.shiro.service.SysRegisterService;
import com.huayang.product.system.service.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 注册验证
 *
 * @author huayang
 */
@Controller
public class SysRegisterController extends BaseController {
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private SysConfigService configService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public AjaxResult ajaxRegister(SysUser user) {
        if (!("true".equals(configService.selectByConfigKey("sys.account.registerUser")))) {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
