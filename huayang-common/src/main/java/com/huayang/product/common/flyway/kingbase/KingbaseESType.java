package com.huayang.product.common.flyway.kingbase;

import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * KingbaseES-specific type.
 */
public class KingbaseESType extends Type<KingbaseESDatabase, KingbaseESSchema> {
    /**
     * Creates a new KingbaseES type.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this type lives in.
     * @param name         The name of the type.
     */
    public KingbaseESType(JdbcTemplate jdbcTemplate, KingbaseESDatabase database, KingbaseESSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TYPE " + database.quote(schema.getName(), name));
    }
}