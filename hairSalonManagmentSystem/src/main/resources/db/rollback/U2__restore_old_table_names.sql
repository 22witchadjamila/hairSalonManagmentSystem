-- Rollback for V2__Standardise_table_names. NOT run automatically: this folder is outside
-- spring.flyway.locations. See docs/DATABASE_MIGRATIONS.md before using it.
--
-- 1. Stop the application.
-- 2. Take a fresh backup (mysqldump) - the database has moved on since the upgrade.
-- 3. Run this script, then deploy the PREVIOUS build of the application (whose entities still
--    use the old table names). Running the new build against the old names would make Hibernate
--    create new, empty tables.
--
-- Foreign keys, indexes and rows move with each table, exactly as they did on the way up.
-- On MySQL for Linux the appointment table was originally "Appointment" (capital A); use that
-- name in the first statement there. On Windows MySQL stores it as "appointment".

ALTER TABLE appointments RENAME TO appointment;
ALTER TABLE loyalty_rewards RENAME TO loyalty_reward;
ALTER TABLE notifications RENAME TO notification;
ALTER TABLE products RENAME TO product_inventory;

-- Tell Flyway V2 is no longer applied, so the new build would run it again if redeployed.
DELETE FROM `flyway_schema_history` WHERE `version` = '2';
