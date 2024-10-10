package com.huayang.product.common.utils.sql;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.exception.UtilException;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;

import java.util.Map;

/**
 * sql操作工具类
 *
 * @author huayang
 */
public class SqlUtil {
    /**
     * 限制orderBy最大长度
     */
    private static final int ORDER_BY_MAX_LENGTH = 500;
    /**
     * 定义常用的 sql关键字
     */
    public static String SQL_REGEX = "and |extractvalue|updatexml|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |+|user()";
    /**
     * 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序）
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /**
     * 检查字符，防止注入绕过
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new UtilException("参数不符合规范，不能进行查询");
        }
        if (StringUtils.length(value) > ORDER_BY_MAX_LENGTH) {
            throw new UtilException("参数已超过最大限制，不能进行查询");
        }
        return value;
    }

    /**
     * 验证 order by 语法是否符合规范
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    /**
     * SQL关键字检查
     */
    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                throw new UtilException("参数存在SQL注入风险");
            }
        }
    }

    /**
     * 兼容数据库语法：处理params中的时间条件
     * 如果是service中使用，则实现BaseService<>接口 {@link BaseService#paramsTimeCondition(LambdaQueryWrapper, SFunction, Map)}
     *
     * @param columnName 字段名称
     * @param params     参数map
     * @return 拼接后的sql
     */
    public static String paramsTime2Sql(String databaseId, String columnName, Map<String, Object> params) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (CollUtil.isNotEmpty(params)) {
            // 开始时间检索
            String beginTime = String.valueOf(params.get(BaseService.BEGIN_TIME));
            // 结束时间检索
            String endTime = String.valueOf(params.get(BaseService.END_TIME));
            // mysql数据库
            if (DbType.MYSQL.getDb().equalsIgnoreCase(databaseId)) {
                if (StringUtils.isNotBlank(beginTime)) {
                    sqlBuilder.append(" and " + columnName + " >= STR_TO_DATE('" + beginTime + "', '%Y-%m-%d')");
                }
                if (StringUtils.isNotBlank(endTime)) {
                    endTime = endTime + " 23:59:59";
                    sqlBuilder.append(" and " + columnName + " <= STR_TO_DATE('" + endTime + "', '%Y-%m-%d %H:%i:%s')");
                }
            }
            // 人大进仓、PostgreSql、Oracle
            else if (DbType.KINGBASE_ES.getDb().equalsIgnoreCase(databaseId)
                    || DbType.POSTGRE_SQL.getDb().equalsIgnoreCase(databaseId)
                    || DbType.ORACLE.getDb().equalsIgnoreCase(databaseId)) {
                if (StringUtils.isNotBlank(beginTime)) {
                    sqlBuilder.append(" and " + columnName + " >= TO_DATE('" + beginTime + "', 'YYYY-MM-DD')");
                }
                if (StringUtils.isNotBlank(endTime)) {
                    endTime = endTime + " 23:59:59";
                    sqlBuilder.append(" and " + columnName + " <= TO_DATE('" + endTime + "', 'YYYY-MM-DD HH24:MI:SS')");
                }
            }
        }
        return sqlBuilder.toString();
    }
}
