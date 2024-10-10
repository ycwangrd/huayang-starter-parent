package com.huayang.product.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huayang.product.common.annotation.Excel;
import com.huayang.product.common.annotation.Excel.ColumnType;
import com.huayang.product.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型表 sys_dict_type
 *
 * @author huayang
 */
@Data
@TableName("sys_dict_type")
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 字典主键
     */
    @TableId
    @Excel(name = "字典主键", cellType = ColumnType.NUMERIC)
    private Long dictId;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 0, max = 100, message = "字典类型名称长度不能超过100个字符")
    @Excel(name = "字典名称")
    private String dictName;

    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型类型长度不能超过100个字符")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "字典类型必须以字母开头，且只能为（小写字母，数字，下滑线）")
    @Excel(name = "字典类型")
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 备注
     */
    private String remark;
}
