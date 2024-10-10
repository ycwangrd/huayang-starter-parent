package com.huayang.product.common.flyway.kingbase;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.strategy.RetryStrategy;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Spring-like template for executing with KingbaseES advisory locks.
 */
@Slf4j
public class KingbaseESAdvisoryLockTemplate {
    private static final long LOCK_MAGIC_NUM =
            (0x46L << 40) // F
                    + (0x6CL << 32) // l
                    + (0x79L << 24) // y
                    + (0x77 << 16) // w
                    + (0x61 << 8) // a
                    + 0x79; // y

    /**
     * The connection for the advisory lock.
     */
    private final JdbcTemplate jdbcTemplate;
    private final long lockNum;

    /**
     * Creates a new advisory lock template for this connection.
     *
     * @param jdbcTemplate  The jdbcTemplate for the connection.
     * @param discriminator A number to discriminate between locks.
     */
    KingbaseESAdvisoryLockTemplate(JdbcTemplate jdbcTemplate, int discriminator) {
        this.jdbcTemplate = jdbcTemplate;
        this.lockNum = LOCK_MAGIC_NUM + discriminator;
    }

    /**
     * Executes this callback with an advisory lock.
     *
     * @param callable The callback to execute.
     * @return The result of the callable code.
     */
    public <T> T execute(Callable<T> callable) {
        RuntimeException rethrow = null;
        try {
            lock();
            return callable.call();
        } catch (SQLException e) {
            rethrow = new FlywaySqlException("Unable to acquire KingbaseES advisory lock", e);
            throw rethrow;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                rethrow = (RuntimeException) e;
            } else {
                rethrow = new FlywayException(e);
            }
            throw rethrow;
        } finally {
            unlock(rethrow);
        }
    }

    private void lock() throws SQLException {
        RetryStrategy strategy = new RetryStrategy();
        strategy.doWithRetries(this::tryLock,
                "Interrupted while attempting to acquire KingbaseES advisory lock",
                "Number of retries exceeded while attempting to acquire KingbaseES advisory lock. " +
                        "Configure the number of retries with the 'lockRetryCount' configuration option: " +
                        FlywayDbWebsiteLinks.LOCK_RETRY_COUNT);
    }

    private boolean tryLock() throws SQLException {
        List<Boolean> results = jdbcTemplate.query("SELECT pg_try_advisory_lock(" + lockNum + ")",
                rs -> rs.getBoolean("pg_try_advisory_lock"));
        return results.size() == 1 && results.get(0);
    }

    private void unlock(RuntimeException rethrow) throws FlywaySqlException {
        try {
            boolean unlocked = jdbcTemplate.queryForBoolean("SELECT pg_advisory_unlock(" + lockNum + ")");
            if (!unlocked) {
                if (rethrow == null) {
                    throw new FlywayException("Unable to release KingbaseES advisory lock");
                } else {
                    log.error("Unable to release KingbaseES advisory lock");
                }
            }
        } catch (SQLException e) {
            if (rethrow == null) {
                throw new FlywaySqlException("Unable to release KingbaseES advisory lock", e);
            } else {
                log.error("Unable to release KingbaseES advisory lock", e);
            }
        }
    }
}