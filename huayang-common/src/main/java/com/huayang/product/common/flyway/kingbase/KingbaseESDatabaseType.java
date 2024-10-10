package com.huayang.product.common.flyway.kingbase;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.authentication.postgres.PgpassFileReader;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

@Slf4j
public class KingbaseESDatabaseType extends BaseDatabaseType {


    @Override
    public String getName() {
        return "KingbaseES";
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        if (url.startsWith("jdbc-secretsmanager:kingbase8:")) {
            throw new FlywayEditionUpgradeRequiredException(Tier.ENTERPRISE, Tier.COMMUNITY, "jdbc-secretsmanager");
        }
        return url.startsWith("jdbc:kingbase8:") || url.startsWith("jdbc:p6spy:kingbase8:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:kingbase8:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "com.kingbase8.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("KingbaseES");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new KingbaseESDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new KingbaseESParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        props.put("applicationName", APPLICATION_NAME);
    }

    @Override
    public boolean detectUserRequiredByUrl(String url) {
        return !url.contains("user=");
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {
        // Postgres supports password in URL
        return !url.contains("password=");
    }

    @Override
    public boolean externalAuthPropertiesRequired(String url, String username, String password) {
        return super.externalAuthPropertiesRequired(url, username, password);
    }

    @Override
    public Properties getExternalAuthProperties(String url, String username) {
        PgpassFileReader pgpassFileReader = new PgpassFileReader();

        if (pgpassFileReader.getPgpassFilePath() != null) {
            log.info(org.flywaydb.core.internal.license.FlywayTeamsUpgradeMessage.generate(
                    "pgpass file '" + pgpassFileReader.getPgpassFilePath() + "'",
                    "use this for database authentication"));
        }
        return super.getExternalAuthProperties(url, username);
    }
}