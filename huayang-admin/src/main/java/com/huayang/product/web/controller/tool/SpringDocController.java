package com.huayang.product.web.controller.tool;

import com.huayang.product.common.core.controller.BaseController;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * swagger 接口
 *
 * @author huayang
 */
@Controller
@RequestMapping("/tool")
public class SpringDocController extends BaseController {
    /**
     * Knife4j 接口文档
     *
     * @return
     */
    @RequiresPermissions("tool:swagger:view")
    @GetMapping("/doc")
    public String doc() {
        return redirect("/doc.html");
    }

    /**
     * Swagger 接口文档
     *
     * @return
     */
    @RequiresPermissions("tool:swagger:view")
    @GetMapping("/swagger")
    public String swagger() {
        return redirect("/swagger-ui.html");
    }
}
