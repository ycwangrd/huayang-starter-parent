package com.huayang.product.generator.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huayang.product.common.annotation.Log;
import com.huayang.product.common.core.controller.BaseController;
import com.huayang.product.common.core.domain.AjaxResult;
import com.huayang.product.common.core.domain.CxSelect;
import com.huayang.product.common.core.page.TableDataInfo;
import com.huayang.product.common.enums.BusinessType;
import com.huayang.product.common.utils.security.PermissionUtils;
import com.huayang.product.generator.domain.GenTable;
import com.huayang.product.generator.domain.GenTableColumn;
import com.huayang.product.generator.service.GenTableColumnService;
import com.huayang.product.generator.service.GenTableService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码生成 操作处理
 *
 * @author huayang
 */
@Slf4j
@Controller
@RequestMapping("/tool/gen")
public class GenController extends BaseController {
    private static Pattern pattern = Pattern.compile("(:\\d+/)(.+)(\\?)");
    private static String schemaName = "";
    private String prefix = "tool/gen";
    @Autowired
    private GenTableService genTableService;
    @Autowired
    private GenTableColumnService genTableColumnService;
    @Autowired
    private DataSource dataSource;

    @RequiresPermissions("tool:gen:view")
    @GetMapping()
    public String gen() {
        return prefix + "/gen";
    }

    /**
     * 查询代码生成列表
     */
    @RequiresPermissions("tool:gen:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo genList(GenTable genTable) {
        startPage();
        List<GenTable> list = genTableService.selectList(genTable);
        return getDataTable(list);
    }

    /**
     * 查询数据库列表
     */
    @RequiresPermissions("tool:gen:list")
    @PostMapping("/db/list")
    @ResponseBody
    public TableDataInfo dataList(GenTable genTable) {
        startPage();
        List<GenTable> list = genTableService.selectDbTableList(genTable, getSchemaName());
        return getDataTable(list);
    }

    /**
     * 查询数据表字段列表
     */
    @RequiresPermissions("tool:gen:list")
    @PostMapping("/column/list")
    @ResponseBody
    public TableDataInfo columnList(Long tableId) {
        TableDataInfo dataInfo = new TableDataInfo();
        LambdaQueryWrapper<GenTableColumn> queryWrapper = new LambdaQueryWrapper<GenTableColumn>()
                .eq(GenTableColumn::getTableId, tableId);
        queryWrapper.orderByAsc(GenTableColumn::getSort);
        List<GenTableColumn> list = genTableColumnService.list(queryWrapper);
        dataInfo.setRows(list);
        dataInfo.setTotal(list.size());
        return dataInfo;
    }

    /**
     * 导入表结构
     */
    @RequiresPermissions("tool:gen:list")
    @GetMapping("/importTable")
    public String importTable() {
        return prefix + "/importTable";
    }

    /**
     * 创建表结构
     */
    @GetMapping("/createTable")
    public String createTable() {
        return prefix + "/createTable";
    }

    /**
     * 导入表结构（保存）
     */
    @RequiresPermissions("tool:gen:list")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    @ResponseBody
    public AjaxResult importTableSave(String tables) {
        String[] tableNames = ConvertUtil.toStrArray(tables);
        // 查询表信息
        List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames, getSchemaName());
        String operName = ConvertUtil.toStr(PermissionUtils.getPrincipalProperty("loginName"));
        genTableService.importGenTable(tableList, operName);
        return AjaxResult.success();
    }

    /**
     * 修改代码生成业务
     */
    @RequiresPermissions("tool:gen:edit")
    @GetMapping("/edit/{tableId}")
    public String edit(@PathVariable("tableId") Long tableId, ModelMap mmap) {
        GenTable table = genTableService.selectByTableId(tableId);
        List<GenTable> genTables = genTableService.selectGenTableAll();
        List<CxSelect> cxSelect = new ArrayList<>();
        for (GenTable genTable : genTables) {
            if (!StringUtils.equals(table.getTableName(), genTable.getTableName())) {
                CxSelect cxTable = new CxSelect(genTable.getTableName(), genTable.getTableName() + '：' + genTable.getTableComment());
                List<CxSelect> cxColumns = new ArrayList<>();
                for (GenTableColumn tableColumn : genTable.getColumns()) {
                    cxColumns.add(new CxSelect(tableColumn.getColumnName(), tableColumn.getColumnName() + '：' + tableColumn.getColumnComment()));
                }
                cxTable.setS(cxColumns);
                cxSelect.add(cxTable);
            }
        }
        mmap.put("table", table);
        mmap.put("data", JSONUtil.toJsonStr(cxSelect));
        return prefix + "/edit";
    }

    /**
     * 修改保存代码生成业务
     */
    @RequiresPermissions("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated GenTable genTable) {
        genTableService.validateEdit(genTable);
        genTableService.updateGenTable(genTable);
        return AjaxResult.success();
    }

    @RequiresPermissions("tool:gen:remove")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        genTableService.deleteGenTableByIds(ids);
        return AjaxResult.success();
    }

    /**
     * 预览代码
     */
    @RequiresPermissions("tool:gen:preview")
    @GetMapping("/preview/{tableId}")
    @ResponseBody
    public AjaxResult preview(@PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return AjaxResult.success(dataMap);
    }

    /**
     * 生成代码（下载方式）
     */
    @RequiresPermissions("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/download/{tableName}")
    public void download(HttpServletResponse response, @PathVariable("tableName") String tableName) throws IOException {
        byte[] data = genTableService.downloadCode(tableName);
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     */
    @RequiresPermissions("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/genCode/{tableName}")
    @ResponseBody
    public AjaxResult genCode(@PathVariable("tableName") String tableName) {
        genTableService.generatorCode(tableName);
        return AjaxResult.success();
    }

    /**
     * 同步数据库
     */
    @RequiresPermissions("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @GetMapping("/synchDb/{tableName}")
    @ResponseBody
    public AjaxResult synchDb(@PathVariable("tableName") String tableName) {
        genTableService.synchDb(tableName);
        return AjaxResult.success();
    }

    /**
     * 批量生成代码
     */
    @RequiresPermissions("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/batchGenCode")
    @ResponseBody
    public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
        String[] tableNames = ConvertUtil.toStrArray(tables);
        byte[] data = genTableService.downloadCode(tableNames);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"hmc-starter-" + LocalDateTime.now() + ".zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }

    /**
     * 获取数据源的SchemaName
     *
     * @return
     */
    private String getSchemaName() {
        try {
            if (StringUtils.isBlank(schemaName)) {
                Connection connection = dataSource.getConnection();
                DatabaseMetaData metaData = connection.getMetaData();
                String jdbcUrl = metaData.getURL();
                Matcher matcher = pattern.matcher(jdbcUrl);
                matcher.find();
                if (matcher.groupCount() == 3) {
                    schemaName = matcher.group(2);
                }
            }
            return schemaName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("无法获取schema，生成代码模块将无法正常运行");
        }
    }
}