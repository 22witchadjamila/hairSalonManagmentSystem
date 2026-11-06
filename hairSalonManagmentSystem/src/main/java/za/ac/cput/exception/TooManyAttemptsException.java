package za.ac.cput.exception;

/** Thrown when an account is temporarily locked after repeated failed sign-ins. Maps to HTTP 429. */
public class TooManyAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyAttemptsException(long retryAfterSeconds) {
        super("Too many failed sign-in attempts. Try again in "
                + Math.max(1, (retryAfterSeconds + 59) / 60) + " minute(s).");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
