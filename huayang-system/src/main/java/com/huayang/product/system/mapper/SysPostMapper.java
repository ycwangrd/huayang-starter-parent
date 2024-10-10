package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.system.domain.SysPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 岗位信息 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {

    /**
     * 根据用户ID查询岗位
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    List<SysPost> selectPostsByUserId(Long userId);
}
