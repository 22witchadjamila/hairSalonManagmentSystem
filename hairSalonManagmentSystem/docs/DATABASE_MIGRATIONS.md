# Database migrations

Schema changes are versioned with **Flyway**. Flyway runs at startup **before** Hibernate, so
a table is renamed before Hibernate looks for it. Without that, `ddl-auto=update` would create
a new, empty table next to the old one and the app would look as if its data had vanished.

| Version | What | Where |
|---|---|---|
| 1 | Baseline: the schema as it existed before Flyway (recorded, not run) | `spring.flyway.baseline-version=1` |
| 2 | Plural snake_case table names | `src/main/java/db/migration/V2__Standardise_table_names.java` |

## V2: table names

| Before | After |
|---|---|
| `Appointment` (stored as `appointment` on Windows MySQL) | `appointments` |
| `loyalty_reward` | `loyalty_rewards` |
| `notification` | `notifications` |
| `product_inventory` | `products` |

Every other table was already plural snake_case: `customers`, `payments`, `promotions`,
`reviews`, `roles`, `services`, `stylists`, `users`. The new `role_permissions` table, which
holds custom-role permissions, is created by Hibernate.

Each table is **renamed**, not copied. Its rows, primary key, indexes and every foreign key that
points at it move with it. That covers `payments`, `reviews` and `notifications` →
`appointments`, among others. The migration is safe in every state a database can be in:

- **Fresh database:** nothing to rename.
- **Old table only:** it is snapshotted to `bak_v2_<new name>`, then renamed.
- **Old table plus an *empty* new one** (Hibernate got there first): the empty one is dropped,
  then the old one is renamed.
- **Both tables have rows:** the migration **stops** with an explanation and changes nothing.
  Merging them is a human decision.

It is idempotent. Running it again, or after a partial failure, only does what is still left.

## Running it against MySQL safely

### 1. Back up (required)

Stop the app in IntelliJ, then run this in a terminal (it asks for the MySQL password):

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u salon_app -p --single-transaction --routines --triggers --result-file=hair_salon_db-backup-before-v2.sql hair_salon_db
```

Use `--result-file` rather than `>`: in Windows PowerShell, `>` re-encodes the dump as UTF-16, which
`mysql` can't load back cleanly. Keep the file somewhere safe (not only in the repo folder) and check that it isn't empty.

Then record the current row counts so you can compare them afterwards:

```sql
SELECT 'appointment', COUNT(*) FROM appointment UNION ALL
SELECT 'loyalty_reward', COUNT(*) FROM loyalty_reward UNION ALL
SELECT 'notification', COUNT(*) FROM notification UNION ALL
SELECT 'product_inventory', COUNT(*) FROM product_inventory UNION ALL
SELECT 'payments', COUNT(*) FROM payments UNION ALL
SELECT 'customers', COUNT(*) FROM customers;
```

### 2. Rehearse on a copy (recommended)

The app reads the database name from `DB_NAME`, so a copy can be tested without touching the
real one:

```sql
CREATE DATABASE hair_salon_db_rehearsal;
```

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u salon_app -p hair_salon_db_rehearsal < hair_salon_db-backup-before-v2.sql
```

In the IntelliJ run configuration, set `DB_NAME=hair_salon_db_rehearsal` and
`SALON_DB_BACKUP_CONFIRMED=2`, then start the app.
Look for this in the log:

```
Migrating schema `hair_salon_db_rehearsal` to version "2 - Standardise table names"
Successfully applied 1 migration ...
```

Click through the app. Then remove `DB_NAME` (so it goes back to `hair_salon_db`) and
`DROP DATABASE hair_salon_db_rehearsal;`.

If `salon_app` isn't allowed to create databases, do this step as `root`.

### 3. Upgrade the real database

**A backup gate protects this step.** If the database already has data and a migration is
pending, the app **refuses to start** and changes nothing until you confirm the backup for that
exact version (`config/FlywayBackupGate`). After taking the backup in step 1, add this to the
IntelliJ run configuration's environment variables:

```
SALON_DB_BACKUP_CONFIRMED=2
```

Start the app. Flyway sees a database with no history table, records it as version 1 (baseline),
then applies V2. That happens once. Later starts log `Schema ... is up to date`, and the variable
can stay or be removed. A future V3 needs `SALON_DB_BACKUP_CONFIRMED=3`, so the old confirmation
can't wave a new change through. Fresh databases, such as the in-memory H2 profile, are never
gated.

For the rehearsal in step 2, set the same variable as well.

### 4. Verify

```sql
SELECT version, description, success FROM flyway_schema_history;          -- 1 (baseline) and 2, success = 1
SHOW TABLES;                                                               -- appointments, products, ... and no old names
SELECT COUNT(*) FROM appointments;                                         -- same as 'appointment' in step 1, likewise for the others
SELECT TABLE_NAME, REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE
 WHERE TABLE_SCHEMA = 'hair_salon_db' AND REFERENCED_TABLE_NAME IS NOT NULL; -- foreign keys now reference appointments / products
```

Once you're satisfied, which can be days later, the snapshot tables can go. **This deletes
data, so do it yourself, deliberately:**

```sql
DROP TABLE bak_v2_appointments, bak_v2_loyalty_rewards, bak_v2_notifications, bak_v2_products;
```

## Rollback

**Option A: undo the renames** (keeps everything written since the upgrade):

1. Stop the app and take a fresh `mysqldump`.
2. Run `src/main/resources/db/rollback/U2__restore_old_table_names.sql`. It renames the four
   tables back and removes the V2 row from `flyway_schema_history`.
3. Deploy the **previous** build. Its entities still use the old names. Starting the new build
   against the old names would just run V2 again.

**Option B: restore the backup** (loses everything written since the upgrade):

1. Stop the app.
2. Recreate the database and load `hair_salon_db-backup-before-v2.sql` with `mysql.exe`, as in
   the rehearsal.
3. Deploy the previous build.

## If the migration fails part-way

MySQL commits each `RENAME` immediately, so a failure can leave some tables renamed. Flyway
then records V2 as failed and refuses to start until that's cleared:

1. Read the error in the log. "Both ... contain rows" means someone ran the new build without
   Flyway, so check which table has the real data.
2. Fix the cause, then remove the failed record: `DELETE FROM flyway_schema_history WHERE version = '2' AND success = 0;`
3. Start the app. V2 skips tables it already renamed and finishes the rest.

## Tests

`src/test/java/db/migration/TableRenameMigrationTest.java` runs V2 through Flyway against a
legacy-shaped H2 database. It checks:

- rows, indexes and foreign keys survive
- snapshots are created
- a second run is a no-op
- a fresh database is a no-op
- an empty Hibernate shell is replaced
- the "both have rows" refusal
- the rollback script itself
