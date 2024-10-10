package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.system.domain.SysUserPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与岗位关联表 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {
}
