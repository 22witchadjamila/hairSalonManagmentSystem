package za.ac.cput.exception;

import java.time.LocalDateTime;

/** Uniform JSON error body returned by {@link GlobalExceptionHandler}. */
public record ApiError(int status, String message, LocalDateTime timestamp) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, LocalDateTime.now());
    }
}
