package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.system.domain.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与菜单关联表 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
