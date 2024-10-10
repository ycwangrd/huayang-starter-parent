package com.huayang.product.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huayang.product.common.annotation.Excel;
import com.huayang.product.common.annotation.Excel.ColumnType;
import com.huayang.product.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置表 sys_config
 *
 * @author huayang
 */
@Data
@TableName("sys_config")
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 参数主键
     */
    @TableId
    @Excel(name = "参数主键", cellType = ColumnType.NUMERIC)
    private Long configId;

    /**
     * 参数名称
     */
    @NotBlank(message = "参数名称不能为空")
    @Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
    @Excel(name = "参数名称")
    private String configName;

    /**
     * 参数键名
     */
    @NotBlank(message = "参数键名长度不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
    @Excel(name = "参数键名")
    private String configKey;

    /**
     * 参数键值
     */
    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
    @Excel(name = "参数键值")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    private String configType;

    /**
     * 备注
     */
    @Excel(name = "备注")
    private String remark;
}
