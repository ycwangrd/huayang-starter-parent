package com.huayang.product.web.controller.system;

import com.huayang.product.common.core.controller.BaseController;
import com.huayang.product.common.core.domain.AjaxResult;
import com.huayang.product.common.core.text.HmcConvertUtil;
import com.huayang.product.common.utils.ServletUtils;
import com.huayang.product.common.utils.security.RsaUtils;
import com.huayang.product.framework.config.ShiroConfig;
import com.huayang.product.framework.web.service.ConfigService;
import com.huayang.product.system.service.SysConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 登录验证
 *
 * @author huayang
 */
@Controller
public class SysLoginController extends BaseController {
    @Autowired
    private ShiroConfig shiroConfig;
    @Autowired
    private ConfigService configService;
    @Autowired
    private SysConfigService sysConfigService;

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, ModelMap mmap) {
        // 如果是Ajax请求，返回Json字符串。
        if (ServletUtils.isAjaxRequest(request)) {
            return ServletUtils.renderString(response, "{\"code\":\"1\",\"msg\":\"未登录或登录超时。请重新登录\"}");
        }
        // 是否开启记住我功能
        mmap.put("isRemembered", shiroConfig.isRememberMe());
        // 是否开启用户注册
        mmap.put("isAllowRegister", HmcConvertUtil.toBool(sysConfigService.selectByConfigKey("sys.account.registerUser"), false));
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public AjaxResult ajaxLogin(String username, String password, Boolean rememberMe) {
        try {
            UsernamePasswordToken token = new UsernamePasswordToken(username, RsaUtils.decryptByPrivateKey(password), rememberMe);
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);
            return success();
        } catch (Exception e) {
            String msg = "用户或密码错误";
            if (StringUtils.isNotEmpty(e.getMessage())) {
                msg = e.getMessage();
            }
            return error(msg);
        }
    }

    @GetMapping("/unauth")
    public String unauth() {
        return "error/unauth";
    }
}
