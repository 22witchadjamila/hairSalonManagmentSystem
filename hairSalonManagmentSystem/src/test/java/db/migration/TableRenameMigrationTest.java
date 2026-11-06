package db.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the real V2 migration through Flyway against an H2 database shaped like the legacy
 * schema (old table names, foreign keys, an index, rows), exactly as an upgrade would.
 */
@DisplayName("V2 table rename migration")
class TableRenameMigrationTest {

    private JdbcDataSource ds;

    @BeforeEach
    void freshDatabase() {
        ds = new JdbcDataSource();
        // MySQL mode, like the real database; a unique name so tests don't share state.
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
    }

    private Flyway flyway() {
        return Flyway.configure().dataSource(ds)
                .baselineOnMigrate(true).baselineVersion("1")
                .locations("classpath:db/migration").load();
    }

    /** The schema as Hibernate created it before the rename. */
    private void createLegacySchema() throws SQLException {
        exec("""
            CREATE TABLE customers (customer_id VARCHAR(255) PRIMARY KEY, first_name VARCHAR(255));
            CREATE TABLE services (service_id VARCHAR(255) PRIMARY KEY, name VARCHAR(255));
            CREATE TABLE appointment (appointment_id VARCHAR(255) PRIMARY KEY, customer_id VARCHAR(255),
                appointment_date DATE, promo_code VARCHAR(255),
                CONSTRAINT fk_appt_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id));
            CREATE INDEX idx_appt_date ON appointment(appointment_date);
            CREATE TABLE payments (payment_id VARCHAR(255) PRIMARY KEY, appointment_id VARCHAR(255),
                CONSTRAINT fk_pay_appt FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id));
            CREATE TABLE notification (notification_id VARCHAR(255) PRIMARY KEY, appointment_id VARCHAR(255),
                CONSTRAINT fk_note_appt FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id));
            CREATE TABLE loyalty_reward (loyalty_id VARCHAR(255) PRIMARY KEY, customer_id VARCHAR(255), points INT,
                CONSTRAINT fk_loyal_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id));
            CREATE TABLE product_inventory (product_id VARCHAR(255) PRIMARY KEY, service_id VARCHAR(255), name VARCHAR(255),
                CONSTRAINT fk_prod_service FOREIGN KEY (service_id) REFERENCES services(service_id));
            INSERT INTO customers VALUES ('c1', 'Jane'), ('c2', 'Sam');
            INSERT INTO services VALUES ('s1', 'Cut');
            INSERT INTO appointment VALUES ('a1', 'c1', DATE '2026-11-01', 'SUMMER10'), ('a2', 'c2', DATE '2026-11-02', NULL);
            INSERT INTO payments VALUES ('p1', 'a1');
            INSERT INTO notification VALUES ('n1', 'a1'), ('n2', 'a2');
            INSERT INTO loyalty_reward VALUES ('l1', 'c1', 120);
            INSERT INTO product_inventory VALUES ('pr1', 's1', 'Shampoo'), ('pr2', 's1', 'Conditioner'), ('pr3', NULL, 'Comb');
            """);
    }

    @Test
    @DisplayName("renames every legacy table, keeping all rows")
    void renamesAndKeepsRows() throws SQLException {
        createLegacySchema();
        flyway().migrate();

        assertFalse(tableExists("APPOINTMENT"));
        assertFalse(tableExists("NOTIFICATION"));
        assertFalse(tableExists("LOYALTY_REWARD"));
        assertFalse(tableExists("PRODUCT_INVENTORY"));
        assertEquals(2, count("appointments"));
        assertEquals(2, count("notifications"));
        assertEquals(1, count("loyalty_rewards"));
        assertEquals(3, count("products"));
        assertEquals("SUMMER10", scalar("SELECT promo_code FROM appointments WHERE appointment_id = 'a1'"));
        assertEquals(1, count("payments"));
    }

    @Test
    @DisplayName("foreign keys still point at the renamed tables and are still enforced")
    void foreignKeysSurvive() throws SQLException {
        createLegacySchema();
        flyway().migrate();

        // A payment for an appointment that doesn't exist is still rejected...
        assertThrows(SQLException.class, () -> exec("INSERT INTO payments VALUES ('p2', 'missing')"));
        // ...and an appointment that payments/notifications reference still can't be deleted.
        assertThrows(SQLException.class, () -> exec("DELETE FROM appointments WHERE appointment_id = 'a1'"));
        // Outgoing keys too: a product can't point at a service that doesn't exist.
        assertThrows(SQLException.class, () -> exec("INSERT INTO products VALUES ('pr9', 'nope', 'X')"));
        // Valid inserts still work against the new names.
        exec("INSERT INTO payments VALUES ('p2', 'a2')");
        assertEquals(2, count("payments"));
    }

    @Test
    @DisplayName("indexes move with the table")
    void indexesSurvive() throws SQLException {
        createLegacySchema();
        flyway().migrate();
        assertEquals(1L, ((Number) scalar("SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES "
                + "WHERE INDEX_NAME = 'IDX_APPT_DATE' AND TABLE_NAME = 'APPOINTMENTS'")).longValue());
    }

    @Test
    @DisplayName("leaves a data snapshot of each old table and records version 2")
    void snapshotsAndHistory() throws SQLException {
        createLegacySchema();
        flyway().migrate();
        assertEquals(2, count("bak_v2_appointments"));
        assertEquals(3, count("bak_v2_products"));
        assertEquals("2", flyway().info().current().getVersion().getVersion());
    }

    @Test
    @DisplayName("running again is a no-op (repeatable upgrade, nothing renamed twice)")
    void secondRunDoesNothing() throws SQLException {
        createLegacySchema();
        flyway().migrate();
        assertEquals(0, flyway().migrate().migrationsExecuted);
        assertEquals(2, count("appointments"));
    }

    @Test
    @DisplayName("fresh database: nothing to rename, nothing created")
    void freshDatabaseIsNoOp() throws SQLException {
        flyway().migrate();
        assertFalse(tableExists("APPOINTMENTS"));
        assertFalse(tableExists("BAK_V2_APPOINTMENTS"));
    }

    @Test
    @DisplayName("an EMPTY new table made by Hibernate is replaced by the real data")
    void replacesEmptyHibernateShell() throws SQLException {
        createLegacySchema();
        exec("CREATE TABLE appointments (appointment_id VARCHAR(255) PRIMARY KEY)");
        flyway().migrate();
        assertEquals(2, count("appointments"));
        assertFalse(tableExists("APPOINTMENT"));
    }

    @Test
    @DisplayName("refuses to guess when old AND new tables both hold rows")
    void refusesWhenBothHaveData() throws SQLException {
        createLegacySchema();
        exec("CREATE TABLE appointments (appointment_id VARCHAR(255) PRIMARY KEY); INSERT INTO appointments VALUES ('x')");
        FlywayException e = assertThrows(FlywayException.class, () -> flyway().migrate());
        StringBuilder messages = new StringBuilder();
        for (Throwable t = e; t != null; t = t.getCause()) messages.append(t.getMessage()).append(" | ");
        assertTrue(messages.toString().contains("contain rows"), messages.toString());
        assertEquals(2, count("appointment"));   // the original data is untouched
    }

    @Test
    @DisplayName("the documented rollback script restores the old names with the data intact")
    void rollbackScriptRestoresOldNames() throws Exception {
        createLegacySchema();
        flyway().migrate();
        String script = new String(getClass().getResourceAsStream("/db/rollback/U2__restore_old_table_names.sql")
                .readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        // Strip comment lines; the script is plain statements separated by semicolons.
        StringBuilder sql = new StringBuilder();
        for (String line : script.split("\\R")) if (!line.trim().startsWith("--")) sql.append(line).append('\n');
        // MySQL quotes with backticks; H2 needs double quotes for the same lower-case table name.
        for (String stmt : sql.toString().split(";")) if (!stmt.isBlank()) exec(stmt.replace('`', '"'));
        assertEquals(2, count("appointment"));
        assertEquals(3, count("product_inventory"));
        assertFalse(tableExists("APPOINTMENTS"));
        assertEquals("1", flyway().info().current().getVersion().getVersion());
    }

    // ---------------------------------------------------------------- helpers

    private void exec(String sql) throws SQLException {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            for (String s : sql.split(";")) if (!s.isBlank()) st.execute(s);
        }
    }

    private Object scalar(String sql) throws SQLException {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getObject(1);
        }
    }

    private long count(String table) throws SQLException {
        return ((Number) scalar("SELECT COUNT(*) FROM " + table)).longValue();
    }

    private boolean tableExists(String upperName) throws SQLException {
        return ((Number) scalar("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"
                + upperName + "'")).longValue() > 0;
    }
}
