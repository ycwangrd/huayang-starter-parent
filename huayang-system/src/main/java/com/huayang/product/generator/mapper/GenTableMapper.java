package com.huayang.product.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.generator.domain.GenTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务 数据层
 *
 * @author huayang
 */
@Mapper
public interface GenTableMapper extends BaseMapper<GenTable> {
    /**
     * 查询据库列表
     *
     * @param genTable 业务信息
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableList(@Param("genTable") GenTable genTable, @Param("schemaName") String schemaName);

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableListByNames(@Param("tableNames") String[] tableNames, @Param("schemaName") String schemaName);

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<GenTable> selectGenTableAll();

    /**
     * 查询表ID业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    GenTable selectByTableId(@Param(value = "tableId") Long tableId);

    /**
     * 查询表名称业务信息
     *
     * @param tableName 表名称
     * @return 业务信息
     */
    GenTable selectGenTableByName(String tableName);
}