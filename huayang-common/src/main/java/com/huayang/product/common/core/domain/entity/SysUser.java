package com.huayang.product.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huayang.product.common.annotation.Excel;
import com.huayang.product.common.annotation.Excel.ColumnType;
import com.huayang.product.common.annotation.Excel.Type;
import com.huayang.product.common.annotation.Excels;
import com.huayang.product.common.core.domain.BaseEntity;
import com.huayang.product.common.xss.Xss;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 用户对象 sys_user
 *
 * @author huayang
 */
@Data
@NoArgsConstructor
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId
    @Excel(name = "用户序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "用户编号")
    private Long userId;

    /**
     * 部门ID
     */
    @Excel(name = "部门编号", type = Type.IMPORT)
    private Long deptId;

    /**
     * 部门父ID
     */
    private Long parentId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 登录名称
     */
    @Xss(message = "登录账号不能包含脚本字符")
    @NotBlank(message = "登录账号不能为空")
    @Size(min = 0, max = 30, message = "登录账号长度不能超过30个字符")
    @Excel(name = "登录名称")
    private String loginName;

    /**
     * 用户名称
     */
    @Xss(message = "用户昵称不能包含脚本字符")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    @Excel(name = "用户名称")
    private String userName;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    @Excel(name = "用户邮箱")
    private String email;

    /**
     * 手机号码
     */
    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    @Excel(name = "手机号码", cellType = ColumnType.TEXT)
    private String phonenumber;

    /**
     * 用户性别
     */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 盐加密
     */
    @JsonIgnore
    private String salt;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    @Excel(name = "最后登录IP", type = Type.EXPORT)
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    /**
     * 密码最后更新时间
     */
    private Date pwdUpdateDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 部门对象
     */
    @TableField(exist = false)
    @Excels({
            @Excel(name = "部门名称", targetAttr = "deptName", type = Type.EXPORT),
            @Excel(name = "部门负责人", targetAttr = "leader", type = Type.EXPORT)
    })
    private SysDept dept;

    @TableField(exist = false)
    private List<SysRole> roles;

    /**
     * 角色组
     */
    @TableField(exist = false)
    private Long[] roleIds;

    /**
     * 岗位组
     */
    @TableField(exist = false)
    private Long[] postIds;

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public SysDept getDept() {
        if (dept == null) {
            dept = new SysDept();
        }
        return dept;
    }
}
