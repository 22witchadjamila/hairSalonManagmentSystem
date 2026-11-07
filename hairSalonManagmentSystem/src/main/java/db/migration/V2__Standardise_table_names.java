package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Renames the four tables that broke the "plural, lower-case snake_case" convention:
 *
 *   Appointment / appointment -> appointments
 *   loyalty_reward            -> loyalty_rewards
 *   notification              -> notifications
 *   product_inventory         -> products
 *
 * RENAME, not create-and-copy: the rows, primary keys, indexes and the foreign keys pointing at
 * these tables (payments.appointment_id, reviews.appointment_id, notifications.appointment_id)
 * all move with the table, on MySQL and on H2.
 *
 * Written in Java rather than plain SQL because it must be safe in every state a database can
 * be in:
 *   - fresh database (H2 profile, new install): the old tables don't exist -> nothing to do,
 *     Hibernate then creates the new names.
 *   - existing database: old table exists, new doesn't -> snapshot, then rename.
 *   - Hibernate already created an EMPTY new table next to the old one (someone ran the new
 *     entity mappings without Flyway) -> drop the empty one, then rename the real one.
 *   - both tables hold rows -> STOP with an explanation. Merging is a human decision.
 * Table names are matched case-insensitively, because MySQL on Windows stores "Appointment" as
 * "appointment" while MySQL on Linux keeps the capital.
 *
 * Safety net: before each rename the old table's rows are copied to bak_v2_<new name>. That is
 * a data snapshot only (no keys or indexes); the real backup is the mysqldump taken before
 * upgrading - see docs/DATABASE_MIGRATIONS.md, which also has the rollback script.
 */
public class V2__Standardise_table_names extends BaseJavaMigration {

    record Rename(List<String> oldNames, String newName) {}

    static final List<Rename> RENAMES = List.of(
            new Rename(List.of("Appointment", "appointment"), "appointments"),
            new Rename(List.of("loyalty_reward", "LoyaltyReward"), "loyalty_rewards"),
            new Rename(List.of("notification", "Notification"), "notifications"),
            new Rename(List.of("product_inventory", "ProductInventory"), "products"));

    @Override
    public void migrate(Context context) throws Exception {
        Connection c = context.getConnection();
        for (Rename r : RENAMES) apply(c, r);
    }

    static void apply(Connection c, Rename r) throws SQLException {
        Optional<String> old = r.oldNames().stream().map(n -> find(c, n)).flatMap(Optional::stream).findFirst();
        if (old.isEmpty()) return;                                  // fresh DB or already renamed
        Optional<String> existingNew = find(c, r.newName());
        String quote = c.getMetaData().getIdentifierQuoteString().trim();
        String oldQ = quote + old.get() + quote;
        try (Statement st = c.createStatement()) {
            if (existingNew.isPresent()) {
                String newQ = quote + existingNew.get() + quote;
                if (count(st, newQ) > 0)
                    throw new IllegalStateException("Both " + old.get() + " and " + existingNew.get()
                            + " contain rows. Refusing to guess which is correct - merge them by hand, then re-run."
                            + " See docs/DATABASE_MIGRATIONS.md.");
                st.execute("DROP TABLE " + newQ);                  // empty shell Hibernate made
            }
            String backup = "bak_v2_" + r.newName();
            if (find(c, backup).isEmpty())
                st.execute("CREATE TABLE " + backup + " AS SELECT * FROM " + oldQ);
            // New name unquoted on purpose: H2 stores it upper-case and MySQL as typed - in both
            // cases exactly what Hibernate's unquoted name resolves to.
            st.execute("ALTER TABLE " + oldQ + " RENAME TO " + r.newName());
        }
    }

    private static long count(Statement st, String table) throws SQLException {
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** The table's stored name if a table matching `name` (ignoring case) exists in this schema. */
    static Optional<String> find(Connection c, String name) {
        try {
            DatabaseMetaData md = c.getMetaData();
            List<String> hits = new ArrayList<>();
            try (ResultSet rs = md.getTables(c.getCatalog(), c.getSchema(), "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String t = rs.getString("TABLE_NAME");
                    if (t.equals(name)) return Optional.of(t);       // exact match wins
                    if (t.toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) hits.add(t);
                }
            }
            return hits.stream().findFirst();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read the table list: " + e.getMessage(), e);
        }
    }
}
