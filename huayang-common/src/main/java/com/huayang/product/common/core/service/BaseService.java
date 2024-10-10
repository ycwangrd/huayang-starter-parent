package com.huayang.product.common.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.huayang.product.common.utils.sql.SqlUtil;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.date.DatePattern;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author wangrd
 * @version 1.0
 * @date 2024年07月30日 星期二 12:28
 * @desc
 */
public interface BaseService<T> {
    /**
     * 查询开始时间
     */
    String BEGIN_TIME = "beginTime";
    /**
     * 查询结束时间
     */
    String END_TIME = "endTime";

    /**
     * 处理params中的时间条件
     * 在mapper.xml中转sql {@link SqlUtil#paramsTime2Sql(String, String, Map)}
     *
     * @param queryWrapper Lambda语法Wrapper
     * @param columnGetter 字段：例如：SysConfig::getCreateTime
     * @param params       参数map
     */
    default void paramsTimeCondition(LambdaQueryWrapper<T> queryWrapper,
                                     SFunction<T, ?> columnGetter, Map<String, Object> params) {
        if (CollUtil.isNotEmpty(params)) {
            String beginTime = String.valueOf(params.get(BEGIN_TIME));
            if (StringUtils.isNotBlank(beginTime)) {
                queryWrapper.ge(columnGetter, LocalDateTime.parse(beginTime, DatePattern.NORM_DATETIME_FORMATTER));
            }
            String endTime = String.valueOf(params.get(END_TIME));
            if (StringUtils.isNotBlank(endTime)) {
                queryWrapper.le(columnGetter, LocalDateTime.parse(endTime, DatePattern.NORM_DATETIME_FORMATTER));
            }
        }
    }
}
