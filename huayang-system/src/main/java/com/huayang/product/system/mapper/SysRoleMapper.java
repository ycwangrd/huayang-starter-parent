package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.common.core.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色表 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    List<SysRole> selectRoleList(SysRole role);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(Long userId);
}
