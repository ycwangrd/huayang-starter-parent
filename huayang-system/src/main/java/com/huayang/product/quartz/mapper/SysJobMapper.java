package com.huayang.product.quartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.quartz.domain.SysJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调度任务信息 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
}
