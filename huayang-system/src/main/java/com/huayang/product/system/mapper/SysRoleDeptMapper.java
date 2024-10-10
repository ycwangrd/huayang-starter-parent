package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.system.domain.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与部门关联表 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
}
