package com.huayang.product.common.flyway.kingbase;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class KingbaseESDatabase extends Database<KingbaseESConnection> {
    public KingbaseESDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected KingbaseESConnection doGetConnection(Connection connection) {
        return new KingbaseESConnection(this, connection);
    }


    public void ensureSupported(Configuration configuration) {
        ensureDatabaseIsRecentEnough("9.0");
        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10", List.of(Tier.ENTERPRISE), configuration);
        recommendFlywayUpgradeIfNecessaryForMajorVersion("14");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String tablespace = configuration.getTablespace() == null
                ? ""
                : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        return "CREATE TABLE " + table + " (" +
                "    \"installed_rank\" INT NOT NULL," +
                "    \"version\" VARCHAR(50)," +
                "    \"description\" VARCHAR(200) NOT NULL," +
                "    \"type\" VARCHAR(20) NOT NULL," +
                "    \"script\" VARCHAR(1000) NOT NULL," +
                "    \"checksum\" INTEGER," +
                "    \"installed_by\" VARCHAR(100) NOT NULL," +
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now()," +
                "    \"execution_time\" INTEGER NOT NULL," +
                "    \"success\" BOOLEAN NOT NULL" +
                ")" + tablespace + ";" +
                (baseline ? getBaselineStatement(table) + ";" : "") +
                "ALTER TABLE " + table + " ADD CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\");" +
                "CREATE INDEX \"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT current_user");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return getOpenQuote() + StringUtils.replaceAll(identifier, getCloseQuote(), getEscapedQuote()) + getCloseQuote();
    }

    @Override
    public String getEscapedQuote() {
        return "\"\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }

    /**
     * This exists to fix this issue: https://github.com/flyway/flyway/issues/2638
     * See https://www.pgpool.net/docs/latest/en/html/runtime-config-load-balancing.html
     */
    @Override
    public String getSelectStatement(Table table) {
        return "/*NO LOAD BALANCE*/"
                + "SELECT " + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_on")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + " FROM " + table
                + " WHERE " + quote("installed_rank") + " > ?"
                + " ORDER BY " + quote("installed_rank");
    }
}