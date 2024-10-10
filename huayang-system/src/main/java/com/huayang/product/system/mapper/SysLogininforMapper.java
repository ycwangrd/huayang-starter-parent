package com.huayang.product.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayang.product.system.domain.SysLogininfor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统访问日志情况信息 数据层
 *
 * @author huayang
 */
@Mapper
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {
    /**
     * 清空系统登录日志
     *
     * @return 结果
     */
    int cleanLogininfor();
}
