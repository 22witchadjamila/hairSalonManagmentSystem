package za.ac.cput.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.exception.InvalidOperationException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordPolicy")
class PasswordPolicyTest {

    @Test
    @DisplayName("accepts a normal password with a letter and a number")
    void acceptsGoodPassword() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("blue-Door7", 8, "lerato"));
    }

    @Test
    @DisplayName("accepts a long passphrase with no digit")
    void acceptsPassphrase() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("correct horse battery", 12, "lerato"));
    }

    @Test
    @DisplayName("refuses a password that is too short for the account type")
    void refusesShort() {
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("abc12", 8, "x"));
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("abcdefg1234", 12, "x"));   // 11 < 12
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate(null, 8, "x"));
    }

    @Test
    @DisplayName("refuses a short password that is only letters or only numbers")
    void refusesNoMix() {
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("onlyletters", 8, "x"));
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("1234567890", 8, "x"));
    }

    @Test
    @DisplayName("refuses well-known weak passwords, in any case")
    void refusesCommon() {
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("Password123", 8, "x"));
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("QWERTY123", 8, "x"));
    }

    @Test
    @DisplayName("refuses a password containing the username")
    void refusesUsername() {
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("Lerato-2026!", 8, "lerato"));
        // very short usernames are ignored - they would match by accident
        assertDoesNotThrow(() -> PasswordPolicy.validate("ab-Door7xyz", 8, "ab"));
    }

    @Test
    @DisplayName("refuses more than 72 bytes (BCrypt would silently ignore the rest)")
    void refusesTooLong() {
        assertThrows(InvalidOperationException.class, () -> PasswordPolicy.validate("a1".repeat(37), 8, "x"));   // 74
        assertDoesNotThrow(() -> PasswordPolicy.validate("a1".repeat(36), 8, "x"));                              // 72
    }
}
