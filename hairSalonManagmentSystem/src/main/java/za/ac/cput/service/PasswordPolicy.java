package za.ac.cput.service;

import za.ac.cput.exception.InvalidOperationException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * One password rule for every way a password can be set: sign-up, a staff account being created,
 * a person changing their own password, and an admin resetting someone else's.
 *
 *   - at least the minimum length for the account type (8 customers, 12 staff)
 *   - at most 72 bytes: BCrypt only reads the first 72, so a longer password would be silently
 *     truncated and "different" passwords could match
 *   - a letter and a digit, unless it is a passphrase of 16+ characters
 *   - not a well-known weak password, and not containing the username
 */
public final class PasswordPolicy {

    public static final int MAX_BYTES = 72;
    private static final int PASSPHRASE_LENGTH = 16;

    private static final Set<String> COMMON = Set.of(
            "password", "password1", "password12", "password123", "passw0rd", "12345678", "123456789",
            "1234567890", "qwerty123", "qwertyuiop", "iloveyou1", "admin123", "administrator", "letmein123",
            "welcome1", "welcome123", "abc12345", "monkey123", "football1", "changeme123", "salon123",
            "hairsalon", "hairsalon1", "judith123");

    private PasswordPolicy() {}

    /** Throws InvalidOperationException with a sentence the sign-up/change forms can show as-is. */
    public static void validate(String raw, int minLength, String username) {
        if (raw == null || raw.length() < minLength)
            throw new InvalidOperationException("Password must be at least " + minLength + " characters.");
        if (raw.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw new InvalidOperationException("Password can be at most " + MAX_BYTES + " characters.");
        boolean letter = raw.chars().anyMatch(Character::isLetter);
        boolean digit = raw.chars().anyMatch(Character::isDigit);
        if (raw.length() < PASSPHRASE_LENGTH && !(letter && digit))
            throw new InvalidOperationException("Password must include at least one letter and one number (or be "
                    + PASSPHRASE_LENGTH + "+ characters long).");
        String lower = raw.toLowerCase(Locale.ROOT);
        if (COMMON.contains(lower))
            throw new InvalidOperationException("That password is too common. Choose something less guessable.");
        if (username != null && username.trim().length() >= 3 && lower.contains(username.trim().toLowerCase(Locale.ROOT)))
            throw new InvalidOperationException("Password can't contain your username.");
    }
}
