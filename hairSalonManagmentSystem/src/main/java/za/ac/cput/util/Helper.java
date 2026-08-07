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
}
