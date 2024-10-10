package com.huayang.product.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.generator.domain.GenTableColumn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 业务字段 数据层
 *
 * @author huayang
 */
@Mapper
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {
    /**
     * 根据表名称查询列信息
     * 直接查询数据库的元数据
     *
     * @param tableName 表名称
     * @return 列信息
     */
    List<GenTableColumn> selectDbColumnsByName(String tableName);
}
