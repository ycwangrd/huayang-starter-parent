package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.system.domain.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 清空操作日志
     */
    void cleanOperLog();
}
