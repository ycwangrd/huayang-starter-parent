package com.huayang.product.framework.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.date.DatePattern;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.db.sql.SqlFormatter;

import java.time.LocalDateTime;

/**
 * @author wangrd
 * 北京华洋峻峰信息工程股份公司
 * https://www.huayanginfo.com ©2008-2020 huayanginfo.com
 * All Rights Reserved.
 * @see MessageFormattingStrategy
 * @since 2020年06月15日 星期一 15:40:51
 * p6spy的日志格式化类
 */
@Slf4j
public class P6SpyLoggerConfig implements MessageFormattingStrategy {
    /**
     * 慢查询超时警告时间
     */
    private final int slowQueryTimeout = 5000;

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        String sqlInfo = "\n---# " + DatePattern.NORM_DATETIME_FORMATTER.format(LocalDateTime.now()) + " | took " + elapsed + "ms | category " + category + " | connection " + connectionId + "\n" + formatSql(sql) + ";";
        if (elapsed > slowQueryTimeout) {
            log.warn("此SQL查询时间为【{}】ms,超过5秒,请注意!!!", elapsed);
        }
        return sqlInfo;
    }

    /**
     * 格式化SQL语句
     *
     * @param sql SQL语句
     * @return
     */
    private String formatSql(String sql) {
        try {
            if (StrUtil.containsAnyIgnoreCase(sql, " from ", " table ", " into ", " set ")) {
                sql = SqlFormatter.format(sql);
            }
        } catch (Exception e) {
        }
        return sql;
    }
}