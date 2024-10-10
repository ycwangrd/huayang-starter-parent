package com.huayang.product.common.core.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huayang.product.common.core.domain.AjaxResult;
import com.huayang.product.common.core.domain.AjaxResult.Type;
import com.huayang.product.common.core.domain.entity.SysUser;
import com.huayang.product.common.core.page.PageDomain;
import com.huayang.product.common.core.page.TableDataInfo;
import com.huayang.product.common.core.page.TableSupport;
import com.huayang.product.common.utils.PageUtils;
import com.huayang.product.common.utils.ServletUtils;
import com.huayang.product.common.utils.ShiroUtils;
import com.huayang.product.common.utils.sql.SqlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.text.StrUtil;

import java.util.List;

/**
 * web层通用数据处理
 *
 * @author huayang
 */
@Slf4j
public class BaseController {
    /**
     * 返回成功数据
     */
    public static AjaxResult success(Object data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage() {
        PageUtils.startPage();
    }

    /**
     * 设置请求排序数据
     */
    protected void startOrderBy() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StrUtil.isNotEmpty(pageDomain.getOrderBy())) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * 清理分页的线程变量
     */
    protected void clearPage() {
        PageUtils.clearPage();
    }

    /**
     * 获取request
     */
    public HttpServletRequest getRequest() {
        return ServletUtils.getRequest();
    }

    /**
     * 获取response
     */
    public HttpServletResponse getResponse() {
        return ServletUtils.getResponse();
    }

    /**
     * 获取session
     */
    public HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 响应请求分页数据
     */
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(0);
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? success() : error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 返回成功
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * 返回错误码消息
     */
    public AjaxResult error(Type type, String message) {
        return new AjaxResult(type, message);
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StrUtil.format("redirect:{}", url);
    }

    /**
     * 获取用户缓存信息
     */
    public SysUser getSysUser() {
        return ShiroUtils.getSysUser();
    }

    /**
     * 设置用户缓存信息
     */
    public void setSysUser(SysUser user) {
        ShiroUtils.setSysUser(user);
    }

    /**
     * 获取登录用户id
     */
    public Long getUserId() {
        return getSysUser().getUserId();
    }

    /**
     * 获取登录用户名
     */
    public String getLoginName() {
        return getSysUser().getLoginName();
    }
}
