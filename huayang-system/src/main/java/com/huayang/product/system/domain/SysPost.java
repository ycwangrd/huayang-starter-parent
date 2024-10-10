package com.huayang.product.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * 岗位表 sys_post
 *
 * @author huayang
 */
@Data
@TableName("sys_post")
@EqualsAndHashCode(callSuper = true)
public class SysPost extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 岗位序号
     */
    @TableId
    @Excel(name = "岗位序号", cellType = ColumnType.NUMERIC)
    private Long postId;

    /**
     * 岗位编码
     */
    @NotBlank(message = "岗位编码不能为空")
    @Size(min = 0, max = 64, message = "岗位编码长度不能超过64个字符")
    @Excel(name = "岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 0, max = 50, message = "岗位名称长度不能超过50个字符")
    @Excel(name = "岗位名称")
    private String postName;

    /**
     * 岗位排序
     */
    @NotBlank(message = "显示顺序不能为空")
    @Excel(name = "岗位排序", cellType = ColumnType.NUMERIC)
    private String postSort;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 备注
     */
    @Excel(name = "备注")
    private String remark;

    /**
     * 用户是否存在此岗位标识 默认不存在
     */
    @TableField(exist = false)
    private boolean flag = false;
}
