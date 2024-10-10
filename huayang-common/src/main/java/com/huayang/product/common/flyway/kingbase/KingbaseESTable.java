package com.huayang.product.common.flyway.kingbase;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * KingbaseES-specific table.
 */
public class KingbaseESTable extends Table<KingbaseESDatabase, KingbaseESSchema> {
    /**
     * Creates a new KingbaseES table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    protected KingbaseESTable(JdbcTemplate jdbcTemplate, KingbaseESDatabase database, KingbaseESSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() {
        try {
            jdbcTemplate.execute("SELECT * FROM " + database.quote(schema.getName(), name));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("SELECT * FROM " + this + " FOR UPDATE");
    }
}