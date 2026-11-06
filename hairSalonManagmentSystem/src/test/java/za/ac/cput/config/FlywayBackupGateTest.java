package za.ac.cput.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FlywayBackupGate - a confirmation names the exact version")
class FlywayBackupGateTest {

    @Test
    @DisplayName("allowed only when the confirmation names the version being applied")
    void confirmationMustNameTheVersion() {
        assertTrue(FlywayBackupGate.allowed("2", "2"));
        assertTrue(FlywayBackupGate.allowed("2", " 2 "));
        assertFalse(FlywayBackupGate.allowed("2", ""));
        assertFalse(FlywayBackupGate.allowed("2", "true"));
        assertFalse(FlywayBackupGate.allowed("3", "2"));   // an old confirmation doesn't cover a new migration
        assertFalse(FlywayBackupGate.allowed("2", null));
    }
}
