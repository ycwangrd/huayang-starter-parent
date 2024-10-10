package com.huayang.product.common.flyway.kingbase;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A KingbaseES COPY FROM STDIN statement.
 */
public class KingbaseESCopyParsedStatement extends ParsedSqlStatement {
    /**
     * Delimiter of COPY statements.
     */
    private static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true


    );

    private final String copyData;

    /**
     * Creates a new KingbaseES COPY ... FROM STDIN statement.
     */
    public KingbaseESCopyParsedStatement(int pos, int line, int col, String sql, String copyData) {
        super(pos, line, col, sql, COPY_DELIMITER, true, true);
        this.copyData = copyData;
    }

    @Override
    public Results execute(JdbcTemplate jdbcTemplate


    ) {
        // #2355: Use reflection to ensure this works in cases where the KingbaseES driver classes were loaded in a
        //        child URLClassLoader instead of the system classloader.
        Object baseConnection;
        Object copyManager;
        Method copyManagerCopyInMethod;
        try {
            Connection connection = jdbcTemplate.getConnection();
            ClassLoader classLoader = connection.getClass().getClassLoader();

            Class<?> baseConnectionClass = classLoader.loadClass("com.kingbase8.core.BaseConnection");
            baseConnection = connection.unwrap(baseConnectionClass);

            Class<?> copyManagerClass = classLoader.loadClass("com.kingbase8.copy.CopyManager");
            Constructor<?> copyManagerConstructor = copyManagerClass.getConstructor(baseConnectionClass);
            copyManagerCopyInMethod = copyManagerClass.getMethod("copyIn", String.class, Reader.class);

            copyManager = copyManagerConstructor.newInstance(baseConnection);
        } catch (Exception e) {
            throw new FlywayException("Unable to find KingbaseES CopyManager class", e);
        }

        Results results = new Results();
        try {
            try {
                Long updateCount = (Long) copyManagerCopyInMethod.invoke(copyManager, getSql(), new StringReader(copyData));
                results.addResult(new Result(updateCount, null, null, getSql()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new SQLException("Unable to execute COPY operation", e);
            }
        } catch (SQLException e) {
            jdbcTemplate.extractErrors(results, e);
        }
        return results;
    }
}