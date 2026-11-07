package za.ac.cput.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Locale;

/**
 * No schema change on a database that holds data until someone confirms a backup exists.
 *
 * Before Flyway migrates, this checks: is there a pending migration, AND does the database
 * already contain tables? If so, startup stops with instructions unless SALON_DB_BACKUP_CONFIRMED
 * names the version being applied (e.g. SALON_DB_BACKUP_CONFIRMED=2). Naming the version means a
 * confirmation given for V2 can't silently wave through a future V3.
 *
 * Fresh databases (the in-memory H2 profile, a new install) have nothing to lose and are never
 * stopped. Nothing is written before the check, so a stopped startup leaves the database exactly
 * as it was - no failed row in flyway_schema_history to clean up.
 *
 * See docs/DATABASE_MIGRATIONS.md.
 */
@Configuration
public class FlywayBackupGate {

    static final String CONFIRM = "SALON_DB_BACKUP_CONFIRMED";
    private static final Logger log = LoggerFactory.getLogger(FlywayBackupGate.class);

    @Bean
    public FlywayMigrationStrategy backupGatedMigration(Environment env) {
        return flyway -> {
            MigrationInfo[] pending = flyway.info().pending();
            if (pending.length > 0 && hasUserTables(flyway)) {
                String target = pending[pending.length - 1].getVersion().getVersion();
                String confirmed = env.getProperty(CONFIRM, "");
                if (!allowed(target, confirmed)) {
                    throw new IllegalStateException(message(pending, target));
                }
                log.warn("Applying schema migration(s) {} - backup confirmed via {}={}.",
                        Arrays.stream(pending).map(m -> "V" + m.getVersion()).toList(), CONFIRM, confirmed);
            }
            flyway.migrate();
        };
    }

    static boolean allowed(String targetVersion, String confirmed) {
        return confirmed != null && confirmed.trim().equals(targetVersion);
    }

    private static String message(MigrationInfo[] pending, String target) {
        return "\n\n  This database has data and a schema change is waiting: "
                + Arrays.stream(pending).map(m -> "V" + m.getVersion() + " " + m.getDescription()).toList()
                + "\n  Nothing has been changed. Before continuing:"
                + "\n    1. Back up the database (mysqldump - see docs/DATABASE_MIGRATIONS.md)."
                + "\n    2. Add the environment variable " + CONFIRM + "=" + target + " to the run configuration."
                + "\n    3. Start the application again.\n";
    }

    /** True when the schema already has tables other than Flyway's own history table. */
    private static boolean hasUserTables(Flyway flyway) {
        try (Connection c = flyway.getConfiguration().getDataSource().getConnection();
             ResultSet rs = c.getMetaData().getTables(c.getCatalog(), c.getSchema(), "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME").toLowerCase(Locale.ROOT);
                if (!name.equals("flyway_schema_history")) return true;
            }
            return false;
        } catch (Exception e) {
            throw new IllegalStateException("Could not inspect the database before migrating: " + e.getMessage(), e);
        }
    }
}
