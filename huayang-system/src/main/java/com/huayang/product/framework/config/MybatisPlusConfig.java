package com.huayang.product.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Mybatis Plus 配置
 *
 * @author huayang
 */
@Slf4j
@EnableTransactionManagement(proxyTargetClass = true)
@Configuration
public class MybatisPlusConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        // 乐观锁插件 https://baomidou.com/guide/interceptor-optimistic-locker.html
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 如果是对全表的删除或更新操作，就会终止该操作 https://baomidou.com/guide/interceptor-block-attack.html
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 分页插件，自动识别数据库类型 https://baomidou.com/guide/interceptor-pagination.html
     */
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型为mysql
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInnerInterceptor.setMaxLimit(-1L);
        return paginationInnerInterceptor;
    }

    /**
     * 提供mybatis支持databaseId切换数据库兼容
     * 该对象必须在此初始化：因为与数据源有直接关系，在其他地方初始化可能会失败
     *
     * @return
     */
    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties prop = new Properties();
        prop.setProperty("Oracle", DbType.ORACLE.getDb());
        prop.setProperty("MySQL", DbType.MYSQL.getDb());
        prop.setProperty("DB2", DbType.DB2.getDb());
        prop.setProperty("H2", DbType.H2.getDb());
        prop.setProperty("HSQL", DbType.HSQL.getDb());
        prop.setProperty("Informix", DbType.INFORMIX.getDb());
        prop.setProperty("Microsoft SQL Server", DbType.SQL_SERVER.getDb());
        prop.setProperty("PostgreSQL", DbType.POSTGRE_SQL.getDb());
        prop.setProperty("Sybase", DbType.SYBASE.getDb());
        prop.setProperty("Adaptive Server Enterprise", DbType.SYBASE.getDb());
        prop.setProperty("KingbaseES", DbType.KINGBASE_ES.getDb());
        databaseIdProvider.setProperties(prop);
        try {
            Connection connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            log.info("MyDatabaseIdProvider-Current DataBase Product Name is: {}", databaseProductName);
            String databaseId = databaseIdProvider.getDatabaseId(dataSource);
            log.info("MyDatabaseIdProvider-Find a matched property value: {}", databaseId);
        } catch (SQLException e) {
            log.error("获取databaseId异常：", e);
        }
        return databaseIdProvider;
    }
}