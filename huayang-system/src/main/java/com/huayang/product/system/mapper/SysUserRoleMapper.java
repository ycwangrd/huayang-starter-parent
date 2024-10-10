package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与角色关联表 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
