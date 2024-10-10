package com.huayang.product.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色和部门关联 sys_role_dept
 *
 * @author huayang
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;

}
