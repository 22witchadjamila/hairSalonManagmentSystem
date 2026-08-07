package za.ac.cput.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Shared helper logic used across every factory and service so validation,
 * ID generation and money/loyalty math live in exactly one place instead of
 * being copy-pasted per entity
 * */
public class Helper {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$");

    // Accepts local SA numbers (0821234567) or international (+27821234567),
    // with optional spaces/dashes.
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+27|0)[ -]?[0-9]{2}[ -]?[0-9]{3}[ -]?[0-9]{4}$");

    // 1 loyalty point earned per R10 spent.
    private static final BigDecimal POINTS_PER_RAND_SPENT = new BigDecimal("10");

    //Loyalty tier thresholds, in total points ever earned.
    private static final int SILVER_THRESHOLD = 500;
    private static final int GOLD_THRESHOLD = 2000;
    private static final int PLATINUM_THRESHOLD = 5000;

    private Helper() {}

    /** True if the string is null, empty, or only whitespace. */
    public static boolean isNullOrEmpty(String str){
        return str == null || str.trim().isEmpty();
    }

    /** True if amount is non-null and strictly greater than zero. */
    public static boolean isValidAmount(BigDecimal amount){
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isPositiveDouble(double value){
        return value > 0;
    }

    public static boolean isPositiveInteger(int value){
        return value > 0;
    }

    /** True if the string looks like a real email address */
    public static boolean isValidEmail(String email){
        return !isNullOrEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** True if the string looks like a valid SA phone number (local or +27 format). */
    public static boolean isValidPhoneNumber(String phoneNumber){
        return !isNullOrEmpty(phoneNumber) &&
                PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }
}
