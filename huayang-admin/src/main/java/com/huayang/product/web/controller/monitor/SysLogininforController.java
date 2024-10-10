package com.huayang.product.web.controller.monitor;

import com.huayang.product.common.annotation.Log;
import com.huayang.product.common.core.controller.BaseController;
import com.huayang.product.common.core.domain.AjaxResult;
import com.huayang.product.common.core.page.TableDataInfo;
import com.huayang.product.common.enums.BusinessType;
import com.huayang.product.common.utils.poi.ExcelUtil;
import com.huayang.product.framework.shiro.service.SysPasswordService;
import com.huayang.product.system.domain.SysLogininfor;
import com.huayang.product.system.service.SysLogininforService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * 系统访问记录
 *
 * @author huayang
 */
@Controller
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {
    private String prefix = "monitor/logininfor";

    @Autowired
    private SysLogininforService logininforService;

    @Autowired
    private SysPasswordService passwordService;

    @RequiresPermissions("monitor:logininfor:view")
    @GetMapping()
    public String logininfor() {
        return prefix + "/logininfor";
    }

    @RequiresPermissions("monitor:logininfor:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysLogininfor logininfor) {
        startPage();
        List<SysLogininfor> list = logininforService.selectList(logininfor);
        return getDataTable(list);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("monitor:logininfor:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysLogininfor logininfor) {
        List<SysLogininfor> list = logininforService.selectList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<>(SysLogininfor.class);
        return util.exportExcel(list, "登录日志");
    }

    @RequiresPermissions("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(logininforService.removeByIds(Arrays.asList(ConvertUtil.toStrArray(ids))));
    }

    @RequiresPermissions("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @ResponseBody
    public AjaxResult clean() {
        logininforService.cleanLogininfor();
        return success();
    }

    @RequiresPermissions("monitor:logininfor:unlock")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @PostMapping("/unlock")
    @ResponseBody
    public AjaxResult unlock(String loginName) {
        passwordService.clearLoginRecordCache(loginName);
        return success();
    }
}
